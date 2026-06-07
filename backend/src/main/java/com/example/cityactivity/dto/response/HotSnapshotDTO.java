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
public class HotSnapshotDTO {

    private Long id;
    private Long activityId;
    private String activityTitle;
    private String activityType;
    private String activityImage;
    private Integer rank;
    private Integer currentParticipants;
    private Integer views;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime activityCreatedAt;
    private LocalDateTime snapshotTime;
    private String timeSlice;
    private Integer rankChange;
}
