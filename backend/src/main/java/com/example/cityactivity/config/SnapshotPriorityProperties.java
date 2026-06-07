package com.example.cityactivity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "activity.snapshot.priority")
@Data
public class SnapshotPriorityProperties {

    private double highTierRatio = 0.2;

    private double mediumTierRatio = 0.3;

    private int highTierIntervalMinutes = 15;

    private int mediumTierIntervalMinutes = 30;

    private int lowTierIntervalMinutes = 60;

    private int soonStartingHours = 6;

    private int criticalStartingHours = 2;

    private double burstGrowthThreshold = 0.3;

    private int burstLookbackMinutes = 30;
}
