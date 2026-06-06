package com.example.cityactivity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorResponse {
    
    private Long id;
    private String name;
    private String avatar;
    private String bio;
    private Integer totalActivities;
    private Integer successRate;
    private Double avgFillSpeedHours;
    private List<CreatorActivityTypeDTO> commonTypes;
    private List<CreatorCommonAreaDTO> commonAreas;
    private List<CreatorReviewTagDTO> reviewTags;
    private List<String> styleTags;
}
