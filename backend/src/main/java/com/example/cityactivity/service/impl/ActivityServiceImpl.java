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
import com.example.cityactivity.service.ActivityService;
import com.example.cityactivity.service.ActivitySnapshotService;
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

    private static final int SNAPSHOT_FRESHNESS_MINUTES = 30;
    
    @Override
    @Transactional
    @CacheEvict(value = {"activities", "hot_activities", "user_activities"}, allEntries = true)
    public ActivityResponse createActivity(ActivityCreateRequest request) {
        publishRateLimitService.checkPublishRate(request.getCreatorId(), request);
        
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
        if ("hot".equals(sortBy)) {
            List<ActivityResponse> snapshotResult = getHotActivitiesByCityFromSnapshot(city);
            if (snapshotResult != null && !snapshotResult.isEmpty()) {
                return snapshotResult;
            }
        }

        List<Activity> activities = activityRepository.findByCity(city);
        sortActivities(activities, sortBy);
        return activities.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivitiesByType(String type, String sortBy) {
        List<Activity> activities = activityRepository.findByType(type);
        sortActivities(activities, sortBy);
        return activities.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivitiesByCityAndType(String city, String type, String sortBy) {
        if ("hot".equals(sortBy)) {
            List<ActivityResponse> snapshotResult = getHotActivitiesByCityAndTypeFromSnapshot(city, type);
            if (snapshotResult != null && !snapshotResult.isEmpty()) {
                return snapshotResult;
            }
        }

        List<Activity> activities = activityRepository.findByCityAndType(city, type);
        sortActivities(activities, sortBy);
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
    public List<ActivityResponse> getHotActivities() {
        List<ActivityResponse> snapshotResult = getGlobalHotActivitiesFromSnapshot(5);
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
    public List<ActivityResponse> getHotActivities(String timeRange) {
        if ("realtime".equals(timeRange)) {
            List<ActivityResponse> snapshotResult = getGlobalHotActivitiesFromSnapshot(5);
            if (snapshotResult != null && !snapshotResult.isEmpty()) {
                return snapshotResult;
            }
        }

        List<Activity> activities;
        LocalDateTime startTime = switch (timeRange) {
            case "realtime" -> LocalDateTime.now().minusHours(24);
            case "3days" -> LocalDateTime.now().minusDays(3);
            case "7days" -> LocalDateTime.now().minusDays(7);
            default -> null;
        };
        
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

    private List<ActivityResponse> getHotActivitiesByCityFromSnapshot(String city) {
        CityHotSnapshotDTO snapshot = activitySnapshotService.getLatestSnapshot(city);
        if (snapshot == null || snapshot.getRankings() == null || snapshot.getRankings().isEmpty()) {
            return null;
        }

        if (!isSnapshotFresh(snapshot.getSnapshotTime())) {
            return null;
        }

        List<Long> activityIds = snapshot.getRankings().stream()
                .map(HotSnapshotDTO::getActivityId)
                .collect(Collectors.toList());

        List<Activity> activities = activityRepository.findByIdsWithCreator(activityIds);

        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getId, a -> a));

        List<Activity> sortedActivities = new ArrayList<>();
        for (Long id : activityIds) {
            Activity activity = activityMap.get(id);
            if (activity != null) {
                sortedActivities.add(activity);
            }
        }

        return sortedActivities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private List<ActivityResponse> getHotActivitiesByCityAndTypeFromSnapshot(String city, String type) {
        CityHotSnapshotDTO snapshot = activitySnapshotService.getLatestSnapshot(city);
        if (snapshot == null || snapshot.getRankings() == null || snapshot.getRankings().isEmpty()) {
            return null;
        }

        if (!isSnapshotFresh(snapshot.getSnapshotTime())) {
            return null;
        }

        List<Long> activityIds = snapshot.getRankings().stream()
                .filter(s -> type.equals(s.getActivityType()))
                .map(HotSnapshotDTO::getActivityId)
                .collect(Collectors.toList());

        if (activityIds.isEmpty()) {
            return null;
        }

        List<Activity> activities = activityRepository.findByIdsWithCreator(activityIds);

        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getId, a -> a));

        List<Activity> sortedActivities = new ArrayList<>();
        for (Long id : activityIds) {
            Activity activity = activityMap.get(id);
            if (activity != null) {
                sortedActivities.add(activity);
            }
        }

        return sortedActivities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean isSnapshotFresh(LocalDateTime snapshotTime) {
        if (snapshotTime == null) {
            return false;
        }
        long minutesSince = ChronoUnit.MINUTES.between(snapshotTime, LocalDateTime.now());
        return minutesSince <= SNAPSHOT_FRESHNESS_MINUTES;
    }

    private List<ActivityResponse> getGlobalHotActivitiesFromSnapshot(int limit) {
        List<HotSnapshotDTO> hotSnapshots = activitySnapshotService.getGlobalHotActivities(limit);
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
            if (activity != null) {
                sortedActivities.add(activity);
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
