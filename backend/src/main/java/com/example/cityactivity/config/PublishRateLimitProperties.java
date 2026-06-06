package com.example.cityactivity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "activity.publish")
public class PublishRateLimitProperties {
    
    private int cooldownSeconds = 300;
    
    private int dailyLimit = 10;
    
    private int similarContentWindowHours = 24;
    
    private double similarTitleThreshold = 0.7;
}
