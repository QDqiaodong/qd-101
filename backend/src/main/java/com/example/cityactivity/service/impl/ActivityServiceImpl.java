package com.example.cityactivity.service.impl;

import com.example.cityactivity.dto.request.ActivityCreateRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.RegistrationStatus;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.exception.ResourceNotFoundException;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.repository.RegistrationRepository;
import com.example.cityactivity.service.ActivityService;
import com.example.cityactivity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    
    private final ActivityRepository activityRepository;
    private final UserService userService;
    private final RegistrationRepository registrationRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = {"activities", "hot_activities", "user_activities"}, allEntries = true)
    public ActivityResponse createActivity(ActivityCreateRequest request) {
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
        sortActivities(activities, sortBy);
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
    @Cacheable(value = "hot_activities", key = "'top5'")
    public List<ActivityResponse> getHotActivities() {
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
