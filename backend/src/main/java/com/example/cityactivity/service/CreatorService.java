package com.example.cityactivity.service;

import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.CreatorResponse;

import java.util.List;

public interface CreatorService {
    
    List<CreatorResponse> getAllCreators(String type, String sortBy);
    
    CreatorResponse getCreatorById(Long id);
    
    List<ActivityResponse> getCreatorActivities(Long creatorId);
}
