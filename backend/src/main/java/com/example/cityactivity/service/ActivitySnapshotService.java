package com.example.cityactivity.service;

import com.example.cityactivity.dto.response.ActivityTrajectoryDTO;
import com.example.cityactivity.dto.response.CityHotSnapshotDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivitySnapshotService {

    void createSnapshotForCity(String city);

    void createSnapshotForAllCities();

    CityHotSnapshotDTO getLatestSnapshot(String city);

    CityHotSnapshotDTO getSnapshotByTimeSlice(String city, String timeSlice);

    List<CityHotSnapshotDTO> getSnapshotsByTimeRange(String city, LocalDateTime startTime, LocalDateTime endTime);

    List<String> getAvailableCities();

    List<String> getAvailableTimeSlices(String city);

    ActivityTrajectoryDTO getActivityTrajectory(Long activityId);

    ActivityTrajectoryDTO getActivityTrajectory(Long activityId, LocalDateTime startTime);

    List<ActivityTrajectoryDTO> getNewlyEnteredActivities(String city, int days);
}
