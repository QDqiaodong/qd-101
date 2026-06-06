package com.example.cityactivity.service.impl;

import com.example.cityactivity.dto.request.RegistrationRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.Registration;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.exception.BusinessException;
import com.example.cityactivity.exception.ResourceNotFoundException;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.repository.RegistrationRepository;
import com.example.cityactivity.repository.UserRepository;
import com.example.cityactivity.service.RegistrationService;
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
public class RegistrationServiceImpl implements RegistrationService {
    
    private final RegistrationRepository registrationRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = {"activities", "hot_activities", "activity_detail", "user_registrations"}, allEntries = true)
    public void register(RegistrationRequest request) {
        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity", request.getActivityId()));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));
        
        if (registrationRepository.findByActivityIdAndUserIdAndCancelledFalse(
                request.getActivityId(), request.getUserId()).isPresent()) {
            throw new BusinessException("您已报名该活动");
        }
        
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException("活动名额已满");
        }
        
        Registration registration = Registration.builder()
                .activity(activity)
                .user(user)
                .registeredAt(LocalDateTime.now())
                .cancelled(false)
                .build();
        
        registrationRepository.save(registration);
        activityRepository.incrementParticipants(request.getActivityId());
        
        log.info("User {} registered for activity {}", request.getUserId(), request.getActivityId());
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"activities", "hot_activities", "activity_detail", "user_registrations"}, allEntries = true)
    public void cancelRegistration(Long activityId, Long userId) {
        Registration registration = registrationRepository.findByActivityIdAndUserIdAndCancelledFalse(activityId, userId)
                .orElseThrow(() -> new BusinessException("未找到报名记录"));
        
        registration.setCancelled(true);
        registration.setCancelledAt(LocalDateTime.now());
        registrationRepository.save(registration);
        
        activityRepository.decrementParticipants(activityId);
        
        log.info("User {} cancelled registration for activity {}", userId, activityId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isRegistered(Long activityId, Long userId) {
        return registrationRepository.findByActivityIdAndUserIdAndCancelledFalse(activityId, userId).isPresent();
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_registrations", key = "'user:' + #userId")
    public List<ActivityResponse> getRegisteredActivities(Long userId) {
        List<Registration> registrations = registrationRepository.findByUserIdAndCancelledFalse(userId);
        
        return registrations.stream()
                .map(r -> {
                    Activity a = r.getActivity();
                    return ActivityResponse.builder()
                            .id(a.getId())
                            .title(a.getTitle())
                            .type(a.getType())
                            .city(a.getCity())
                            .location(a.getLocation())
                            .time(a.getTime())
                            .maxParticipants(a.getMaxParticipants())
                            .currentParticipants(a.getCurrentParticipants())
                            .description(a.getDescription())
                            .requirements(a.getRequirements())
                            .image(a.getImage())
                            .views(a.getViews())
                            .createdAt(a.getCreatedAt())
                            .creatorId(a.getCreator().getId())
                            .creatorName(a.getCreator().getName())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
