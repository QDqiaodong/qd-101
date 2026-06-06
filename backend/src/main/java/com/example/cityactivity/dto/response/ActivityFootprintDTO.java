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
public class ActivityFootprintDTO {
    
    private Long id;
    
    private String type;
    
    private String title;
    
    private String activityType;
    
    private String city;
    
    private String location;
    
    private String image;
    
    private Long activityId;
    
    private LocalDateTime activityTime;
    
    private LocalDateTime eventTime;
    
    private String eventType;
    
    private String description;
}
