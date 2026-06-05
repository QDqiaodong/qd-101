package com.example.cityactivity.service;

import com.example.cityactivity.dto.request.ActivityCreateRequest;
import com.example.cityactivity.dto.response.ActivityResponse;

import java.util.List;

public interface ActivityService {
    ActivityResponse createActivity(ActivityCreateRequest request);
    ActivityResponse getActivityById(Long id);
    List<ActivityResponse> getAllActivities(String sortBy);
    List<ActivityResponse> getActivitiesByCity(String city, String sortBy);
    List<ActivityResponse> getActivitiesByType(String type, String sortBy);
    List<ActivityResponse> getActivitiesByCityAndType(String city, String type, String sortBy);
    List<ActivityResponse> getActivitiesByCreator(Long creatorId);
    List<ActivityResponse> getHotActivities();
    void incrementViews(Long id);
}
