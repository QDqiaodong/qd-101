package com.example.cityactivity.controller;

import com.example.cityactivity.dto.request.RegistrationRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.ApiResponse;
import com.example.cityactivity.dto.response.RegistrationStatusDTO;
import com.example.cityactivity.dto.response.WaitlistUserResponse;
import com.example.cityactivity.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    
    private final RegistrationService registrationService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegistrationRequest request) {
        registrationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", null));
    }
    
    @DeleteMapping(path = {"", "/cancel"})
    public ResponseEntity<ApiResponse<Void>> cancelRegistration(
            @RequestParam Long activityId,
            @RequestParam Long userId) {
        registrationService.cancelRegistration(activityId, userId);
        return ResponseEntity.ok(ApiResponse.success("Registration cancelled", null));
    }
    
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkRegistration(
            @RequestParam Long activityId,
            @RequestParam Long userId) {
        boolean registered = registrationService.isRegistered(activityId, userId);
        return ResponseEntity.ok(ApiResponse.success(registered));
    }
    
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<RegistrationStatusDTO>> getRegistrationStatus(
            @RequestParam Long activityId,
            @RequestParam Long userId) {
        RegistrationStatusDTO status = registrationService.getRegistrationStatus(activityId, userId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    
    @GetMapping("/waitlist-position")
    public ResponseEntity<ApiResponse<Integer>> getWaitlistPosition(
            @RequestParam Long activityId,
            @RequestParam Long userId) {
        Integer position = registrationService.getWaitlistPosition(activityId, userId);
        return ResponseEntity.ok(ApiResponse.success(position));
    }
    
    @GetMapping("/waitlist/{activityId}")
    public ResponseEntity<ApiResponse<List<WaitlistUserResponse>>> getWaitlist(
            @PathVariable Long activityId) {
        List<WaitlistUserResponse> waitlist = registrationService.getWaitlist(activityId);
        return ResponseEntity.ok(ApiResponse.success(waitlist));
    }
    
    @GetMapping("/waitlist-count/{activityId}")
    public ResponseEntity<ApiResponse<Integer>> getWaitlistCount(
            @PathVariable Long activityId) {
        Integer count = registrationService.getWaitlistCount(activityId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getRegisteredActivities(@PathVariable Long userId) {
        List<ActivityResponse> activities = registrationService.getRegisteredActivities(userId);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
