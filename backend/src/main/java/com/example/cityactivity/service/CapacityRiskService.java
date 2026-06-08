package com.example.cityactivity.service;

import com.example.cityactivity.dto.response.CapacityRiskCheckResult;

public interface CapacityRiskService {

    CapacityRiskCheckResult checkActivityCapacity(String activityType, int maxParticipants);

    CapacityRiskCheckResult checkCapacityExpansion(String activityType, int oldMaxParticipants, int newMaxParticipants);
}
