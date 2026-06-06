package com.example.cityactivity.service.impl;

import com.example.cityactivity.dto.response.ActivityFootprintDTO;
import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.Registration;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.exception.ResourceNotFoundException;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.repository.RegistrationRepository;
import com.example.cityactivity.repository.UserRepository;
import com.example.cityactivity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final RegistrationRepository registrationRepository;
    
    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
    
    @Override
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ActivityFootprintDTO> getUserActivityFootprints(Long userId) {
        List<ActivityFootprintDTO> footprints = new ArrayList<>();
        
        List<Activity> createdActivities = activityRepository.findByCreatorId(userId);
        for (Activity activity : createdActivities) {
            footprints.add(buildFootprint(activity, "PUBLISHED", activity.getCreatedAt(),
                    "你发布了活动 \"" + activity.getTitle() + "\""));
            
            if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
                footprints.add(buildFootprint(activity, "FULL", activity.getTime(),
                        "活动 \"" + activity.getTitle() + "\" 已报满"));
            } else if (activity.getCurrentParticipants() >= activity.getMaxParticipants() * 0.5) {
                footprints.add(buildFootprint(activity, "CONFIRMED", activity.getTime(),
                        "活动 \"" + activity.getTitle() + "\" 已成局"));
            }
            
            if (activity.getTime().isBefore(LocalDateTime.now())) {
                footprints.add(buildFootprint(activity, "EXPIRED", activity.getTime(),
                        "活动 \"" + activity.getTitle() + "\" 已结束"));
            }
        }
        
        List<Registration> registrations = registrationRepository.findByUserId(userId);
        for (Registration registration : registrations) {
            Activity activity = registration.getActivity();
            
            if (!registration.getCancelled()) {
                footprints.add(buildFootprint(activity, "REGISTERED", registration.getRegisteredAt(),
                        "你报名了活动 \"" + activity.getTitle() + "\""));
            } else {
                footprints.add(buildFootprint(activity, "REGISTERED", registration.getRegisteredAt(),
                        "你报名了活动 \"" + activity.getTitle() + "\""));
                
                LocalDateTime cancelTime = registration.getCancelledAt() != null 
                        ? registration.getCancelledAt() 
                        : registration.getRegisteredAt().plusHours(1);
                footprints.add(buildFootprint(activity, "CANCELLED", cancelTime,
                        "你取消了活动 \"" + activity.getTitle() + "\" 的报名"));
            }
        }
        
        footprints.sort(Comparator.comparing(ActivityFootprintDTO::getEventTime).reversed());
        
        long id = 1;
        for (ActivityFootprintDTO footprint : footprints) {
            footprint.setId(id++);
        }
        
        return footprints;
    }
    
    private ActivityFootprintDTO buildFootprint(Activity activity, String eventType, 
                                                 LocalDateTime eventTime, String description) {
        return ActivityFootprintDTO.builder()
                .activityId(activity.getId())
                .title(activity.getTitle())
                .activityType(activity.getType())
                .city(activity.getCity())
                .location(activity.getLocation())
                .image(activity.getImage())
                .activityTime(activity.getTime())
                .eventType(eventType)
                .eventTime(eventTime)
                .description(description)
                .build();
    }
}
