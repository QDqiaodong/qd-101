package com.example.cityactivity.controller;

import com.example.cityactivity.dto.request.ActivityCreateRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.ApiResponse;
import com.example.cityactivity.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityService activityService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(@Valid @RequestBody ActivityCreateRequest request) {
        ActivityResponse activity = activityService.createActivity(request);
        return ResponseEntity.ok(ApiResponse.success("Activity created", activity));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivityById(@PathVariable Long id) {
        activityService.incrementViews(id);
        ActivityResponse activity = activityService.getActivityById(id);
        return ResponseEntity.ok(ApiResponse.success(activity));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getActivities(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "newest") String sortBy) {
        
        List<ActivityResponse> activities;
        
        if (city != null && type != null) {
            activities = activityService.getActivitiesByCityAndType(city, type, sortBy);
        } else if (city != null) {
            activities = activityService.getActivitiesByCity(city, sortBy);
        } else if (type != null) {
            activities = activityService.getActivitiesByType(type, sortBy);
        } else {
            activities = activityService.getAllActivities(sortBy);
        }
        
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
    
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getActivitiesByCreator(@PathVariable Long creatorId) {
        List<ActivityResponse> activities = activityService.getActivitiesByCreator(creatorId);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
    
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getHotActivities() {
        List<ActivityResponse> activities = activityService.getHotActivities();
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
