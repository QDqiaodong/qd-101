package com.example.cityactivity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityActivityScoreDTO {

    private String city;

    private long activityCount;

    private long totalParticipants;

    private long totalViews;

    private double score;

    private PriorityTier priorityTier;

    public enum PriorityTier {
        HIGH,
        MEDIUM,
        LOW
    }
}
