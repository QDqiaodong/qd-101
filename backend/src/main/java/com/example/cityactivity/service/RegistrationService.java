package com.example.cityactivity.service;

import com.example.cityactivity.dto.request.RegistrationRequest;
import com.example.cityactivity.dto.response.ActivityResponse;

import java.util.List;

public interface RegistrationService {
    void register(RegistrationRequest request);
    void cancelRegistration(Long activityId, Long userId);
    boolean isRegistered(Long activityId, Long userId);
    List<ActivityResponse> getRegisteredActivities(Long userId);
}
