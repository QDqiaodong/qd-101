package com.example.cityactivity.service;

import com.example.cityactivity.dto.request.RegistrationRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.RegistrationStatusDTO;
import com.example.cityactivity.dto.response.WaitlistUserResponse;

import java.util.List;

public interface RegistrationService {
    void register(RegistrationRequest request);
    void cancelRegistration(Long activityId, Long userId);
    boolean isRegistered(Long activityId, Long userId);
    List<ActivityResponse> getRegisteredActivities(Long userId);
    
    RegistrationStatusDTO getRegistrationStatus(Long activityId, Long userId);
    Integer getWaitlistPosition(Long activityId, Long userId);
    List<WaitlistUserResponse> getWaitlist(Long activityId);
    Integer getWaitlistCount(Long activityId);
}
