package com.example.cityactivity.controller;

import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.ApiResponse;
import com.example.cityactivity.dto.response.CreatorResponse;
import com.example.cityactivity.service.CreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/creators")
@RequiredArgsConstructor
public class CreatorController {
    
    private final CreatorService creatorService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CreatorResponse>>> getCreators(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "popular") String sortBy) {
        
        List<CreatorResponse> creators = creatorService.getAllCreators(type, sortBy);
        return ResponseEntity.ok(ApiResponse.success(creators));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CreatorResponse>> getCreatorById(@PathVariable Long id) {
        CreatorResponse creator = creatorService.getCreatorById(id);
        return ResponseEntity.ok(ApiResponse.success(creator));
    }
    
    @GetMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getCreatorActivities(@PathVariable("id") Long creatorId) {
        List<ActivityResponse> activities = creatorService.getCreatorActivities(creatorId);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
