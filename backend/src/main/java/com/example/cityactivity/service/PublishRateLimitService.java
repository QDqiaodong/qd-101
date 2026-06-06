package com.example.cityactivity.service;

import com.example.cityactivity.dto.request.ActivityCreateRequest;

public interface PublishRateLimitService {
    void checkPublishRate(Long creatorId, ActivityCreateRequest request);
}
