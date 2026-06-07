package com.example.cityactivity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_hot_snapshots", indexes = {
    @Index(name = "idx_snapshot_city_time", columnList = "city, snapshotTime DESC"),
    @Index(name = "idx_snapshot_activity", columnList = "activityId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityHotSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Long activityId;

    @Column(nullable = false)
    private String activityTitle;

    @Column(nullable = false)
    private String activityType;

    private String activityImage;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private Integer currentParticipants;

    @Column(nullable = false)
    private Integer views;

    private Long creatorId;

    private String creatorName;

    @Column(nullable = false)
    private LocalDateTime activityCreatedAt;

    @Column(nullable = false)
    private LocalDateTime snapshotTime;

    @Column(nullable = false)
    private String timeSlice;
}
