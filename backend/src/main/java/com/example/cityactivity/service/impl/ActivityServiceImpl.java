package com.example.cityactivity.service.impl;

import com.example.cityactivity.dto.request.ActivityCreateRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.CityHotSnapshotDTO;
import com.example.cityactivity.dto.response.HotSnapshotDTO;
import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.RegistrationStatus;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.exception.ResourceNotFoundException;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.repository.RegistrationRepository;
import com.example.cityactivity.dto.response.CapacityRiskCheckResult;
import com.example.cityactivity.dto.response.ContentReviewResult;
import com.example.cityactivity.enums.RiskLevel;
import com.example.cityactivity.exception.BusinessException;
import com.example.cityactivity.service.ActivityService;
import com.example.cityactivity.service.ActivitySnapshotService;
import com.example.cityactivity.service.CapacityRiskService;
import com.example.cityactivity.service.ContentReviewService;
import com.example.cityactivity.service.PublishRateLimitService;
import com.example.cityactivity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final UserService userService;
    private final RegistrationRepository registrationRepository;
    private final PublishRateLimitService publishRateLimitService;
    private final ActivitySnapshotService activitySnapshotService;
    private final ContentReviewService contentReviewService;
    private final CapacityRiskService capacityRiskService;

    private static final int SNAPSHOT_FRESHNESS_MINUTES = 30;
    
    @Override
    @Transactional
    @CacheEvict(value = {"activities", "hot_activities", "user_activities"}, allEntries = true)
    public ActivityResponse createActivity(ActivityCreateRequest request) {
        publishRateLimitService.checkPublishRate(request.getCreatorId(), request);

        ContentReviewResult reviewResult = contentReviewService.reviewActivityContent(request);
        if (!reviewResult.isPassed()) {
            log.warn("Activity content review failed for creator {}: {}",
                    request.getCreatorId(), reviewResult.getSuggestion());
            if (reviewResult.getOverallRiskLevel() == RiskLevel.HIGH) {
                throw new BusinessException("内容审核未通过：" + reviewResult.getSuggestion());
            }
            if (reviewResult.getOverallRiskLevel() == RiskLevel.MEDIUM) {
                throw new BusinessException("内容存在风险，需人工审核：" + reviewResult.getSuggestion());
            }
        }

        CapacityRiskCheckResult capacityResult = capacityRiskService.checkActivityCapacity(
                request.getType(), request.getMaxParticipants());
        if (!capacityResult.isPassed()) {
            log.warn("Activity capacity risk check failed for type {} with {} participants: {}",
                    request.getType(), request.getMaxParticipants(), capacityResult.getSuggestion());
            if (capacityResult.getOverallRiskLevel() == RiskLevel.HIGH) {
                throw new BusinessException("活动人数不符合风控规则：" + capacityResult.getSuggestion());
            }
            if (capacityResult.getOverallRiskLevel() == RiskLevel.MEDIUM) {
                throw new BusinessException("活动人数超出合理范围，需人工审核：" + capacityResult.getSuggestion());
            }
        } else if (capacityResult.getOverallRiskLevel() == RiskLevel.LOW) {
            log.info("Activity capacity warning for type {} with {} participants: {}",
                    request.getType(), request.getMaxParticipants(), capacityResult.getSuggestion());
        }
        
        User creator = userService.findById(request.getCreatorId());
        
        Activity activity = Activity.builder()
                .title(request.getTitle())
                .type(request.getType())
                .city(request.getCity())
                .location(request.getLocation())
                .time(request.getTime())
                .maxParticipants(request.getMaxParticipants())
                .currentParticipants(0)
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .image(request.getImage())
                .views(0)
                .createdAt(LocalDateTime.now())
                .creator(creator)
                .build();
        
        Activity saved = activityRepository.save(activity);
        log.info("Created activity: {}", saved.getId());
        return toResponse(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activity_detail", key = "#id")
    public ActivityResponse getActivityById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
        return toResponse(activity);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activities", key = "'all:' + #sortBy")
    public List<ActivityResponse> getAllActivities(String sortBy) {
        List<Activity> activities = switch (sortBy) {
            case "popular" -> activityRepository.findAllOrderByViewsDesc();
            case "hot" -> activityRepository.findAllOrderByParticipantsDesc();
            default -> activityRepository.findAllOrderByCreatedAtDesc();
        };
        return activities.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activities", key = "'city:' + #city + ':' + #sortBy")
    public List<ActivityResponse> getActivitiesByCity(String city, String sortBy) {
        List<Activity> activities = activityRepository.findByCity(city);

        if ("hot".equals(sortBy)) {
            CityHotSnapshotDTO snapshot = getFreshCitySnapshot(city);
            if (snapshot != null) {
                activities = sortActivitiesBySnapshot(activities, snapshot);
            } else {
                sortActivities(activities, sortBy);
            }
        } else {
            sortActivities(activities, sortBy);
        }

        return activities.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activities", key = "'type:' + #type + ':' + #sortBy")
    public List<ActivityResponse> getActivitiesByType(String type, String sortBy) {
        List<Activity> activities = activityRepository.findByType(type);
        sortActivities(activities, sortBy);
        return activities.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activities", key = "'city_type:' + #city + ':' + #type + ':' + #sortBy")
    public List<ActivityResponse> getActivitiesByCityAndType(String city, String type, String sortBy) {
        List<Activity> activities = activityRepository.findByCityAndType(city, type);

        if ("hot".equals(sortBy)) {
            CityHotSnapshotDTO snapshot = getFreshCitySnapshot(city);
            if (snapshot != null) {
                activities = sortActivitiesBySnapshot(activities, snapshot);
            } else {
                sortActivities(activities, sortBy);
            }
        } else {
            sortActivities(activities, sortBy);
        }

        return activities.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_activities", key = "'creator:' + #creatorId")
    public List<ActivityResponse> getActivitiesByCreator(Long creatorId) {
        List<Activity> activities = activityRepository.findByCreatorId(creatorId);
        return activities.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "hot_activities", key = "'top5'")
    public List<ActivityResponse> getHotActivities() {
        List<ActivityResponse> snapshotResult = getGlobalHotActivitiesFromSnapshot(5, null);
        if (snapshotResult != null && !snapshotResult.isEmpty()) {
            return snapshotResult;
        }

        List<Activity> activities = activityRepository.findAllOrderByParticipantsDesc();
        return activities.stream()
                .limit(5)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "hot_activities", key = "'top5:' + #timeRange")
    public List<ActivityResponse> getHotActivities(String timeRange) {
        LocalDateTime startTime = switch (timeRange) {
            case "realtime" -> LocalDateTime.now().minusHours(24);
            case "3days" -> LocalDateTime.now().minusDays(3);
            case "7days" -> LocalDateTime.now().minusDays(7);
            default -> null;
        };

        if (startTime != null) {
            List<ActivityResponse> snapshotResult = getGlobalHotActivitiesFromSnapshot(5, startTime);
            if (snapshotResult != null && snapshotResult.size() >= 3) {
                return snapshotResult;
            }
        } else {
            List<ActivityResponse> snapshotResult = getGlobalHotActivitiesFromSnapshot(5, null);
            if (snapshotResult != null && !snapshotResult.isEmpty()) {
                return snapshotResult;
            }
        }

        List<Activity> activities;
        if (startTime != null) {
            activities = activityRepository.findHotActivitiesSince(startTime);
        } else {
            activities = activityRepository.findAllOrderByParticipantsDesc();
        }
        
        return activities.stream()
                .limit(5)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"activity_detail", "activities", "hot_activities"}, allEntries = true)
    public void incrementViews(Long id) {
        activityRepository.incrementViews(id);
        log.debug("Incremented views for activity: {}", id);
    }
    
    private void sortActivities(List<Activity> activities, String sortBy) {
        switch (sortBy) {
            case "popular" -> activities.sort((a, b) -> b.getViews() - a.getViews());
            case "hot" -> activities.sort((a, b) -> b.getCurrentParticipants() - a.getCurrentParticipants());
            default -> activities.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }
    }

    private CityHotSnapshotDTO getFreshCitySnapshot(String city) {
        CityHotSnapshotDTO snapshot = activitySnapshotService.getLatestSnapshot(city);
        if (snapshot == null || snapshot.getRankings() == null || snapshot.getRankings().isEmpty()) {
            return null;
        }
        if (!isSnapshotFresh(snapshot.getSnapshotTime())) {
            return null;
        }
        return snapshot;
    }

    private List<Activity> sortActivitiesBySnapshot(List<Activity> activities, CityHotSnapshotDTO snapshot) {
        List<HotSnapshotDTO> rankings = snapshot.getRankings();
        Map<Long, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < rankings.size(); i++) {
            rankMap.put(rankings.get(i).getActivityId(), i);
        }

        List<Activity> snapshotActivities = new ArrayList<>();
        List<Activity> remainingActivities = new ArrayList<>();

        for (Activity activity : activities) {
            if (rankMap.containsKey(activity.getId())) {
                snapshotActivities.add(activity);
            } else {
                remainingActivities.add(activity);
            }
        }

        snapshotActivities.sort(Comparator.comparingInt(a -> rankMap.get(a.getId())));

        remainingActivities.sort((a, b) -> b.getCurrentParticipants() - a.getCurrentParticipants());

        List<Activity> result = new ArrayList<>(snapshotActivities);
        result.addAll(remainingActivities);
        return result;
    }

    private boolean isSnapshotFresh(LocalDateTime snapshotTime) {
        if (snapshotTime == null) {
            return false;
        }
        long minutesSince = ChronoUnit.MINUTES.between(snapshotTime, LocalDateTime.now());
        return minutesSince <= SNAPSHOT_FRESHNESS_MINUTES;
    }

    private List<ActivityResponse> getGlobalHotActivitiesFromSnapshot(int limit, LocalDateTime startTime) {
        int fetchLimit = startTime != null ? limit * 3 : limit;
        List<HotSnapshotDTO> hotSnapshots = activitySnapshotService.getGlobalHotActivities(fetchLimit);
        if (hotSnapshots == null || hotSnapshots.isEmpty()) {
            return null;
        }

        List<Long> activityIds = hotSnapshots.stream()
                .map(HotSnapshotDTO::getActivityId)
                .collect(Collectors.toList());

        List<Activity> activities = activityRepository.findByIdsWithCreator(activityIds);

        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getId, a -> a));

        List<Activity> sortedActivities = new ArrayList<>();
        for (Long id : activityIds) {
            Activity activity = activityMap.get(id);
            if (activity == null) {
                continue;
            }
            if (startTime != null && activity.getCreatedAt().isBefore(startTime)) {
                continue;
            }
            sortedActivities.add(activity);
            if (sortedActivities.size() >= limit) {
                break;
            }
        }

        return sortedActivities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    private ActivityResponse toResponse(Activity activity) {
        Integer waitlistCount = (int) registrationRepository.countByActivityIdAndStatus(
                activity.getId(), RegistrationStatus.WAITLISTED);
        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .type(activity.getType())
                .city(activity.getCity())
                .location(activity.getLocation())
                .time(activity.getTime())
                .maxParticipants(activity.getMaxParticipants())
                .currentParticipants(activity.getCurrentParticipants())
                .description(activity.getDescription())
                .requirements(activity.getRequirements())
                .image(activity.getImage())
                .views(activity.getViews())
                .createdAt(activity.getCreatedAt())
                .creatorId(activity.getCreator().getId())
                .creatorName(activity.getCreator().getName())
                .waitlistCount(waitlistCount)
                .build();
    }
}
