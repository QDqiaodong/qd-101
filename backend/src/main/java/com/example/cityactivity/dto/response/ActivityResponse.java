package com.example.cityactivity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    
    private Long id;
    private String title;
    private String type;
    private String city;
    private String location;
    private LocalDateTime time;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String description;
    private String requirements;
    private String image;
    private Integer views;
    private LocalDateTime createdAt;
    private Long creatorId;
    private String creatorName;
    private Integer waitlistCount;
}
