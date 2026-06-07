package com.example.cityactivity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTrajectoryDTO {

    private Long activityId;
    private String activityTitle;
    private String activityType;
    private String city;
    private String activityImage;
    private LocalDateTime activityCreatedAt;
    private LocalDateTime firstEnteredHotListAt;
    private Integer bestRank;
    private LocalDateTime bestRankAt;
    private Integer currentRank;
    private Integer totalSnapshots;
    private List<TrajectoryPoint> trajectoryPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrajectoryPoint {
        private LocalDateTime snapshotTime;
        private String timeSlice;
        private Integer rank;
        private Integer currentParticipants;
        private Integer views;
    }
}
