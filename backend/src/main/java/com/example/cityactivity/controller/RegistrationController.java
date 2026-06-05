package com.example.cityactivity.controller;

import com.example.cityactivity.dto.request.RegistrationRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.ApiResponse;
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
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getRegisteredActivities(@PathVariable Long userId) {
        List<ActivityResponse> activities = registrationService.getRegisteredActivities(userId);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
