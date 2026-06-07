package com.example.cityactivity.controller;

import com.example.cityactivity.dto.response.ActivityTrajectoryDTO;
import com.example.cityactivity.dto.response.ApiResponse;
import com.example.cityactivity.dto.response.CityHotSnapshotDTO;
import com.example.cityactivity.service.ActivitySnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/snapshots")
@RequiredArgsConstructor
public class ActivitySnapshotController {

    private final ActivitySnapshotService activitySnapshotService;

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableCities() {
        List<String> cities = activitySnapshotService.getAvailableCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    @GetMapping("/cities/{city}/time-slices")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableTimeSlices(@PathVariable String city) {
        List<String> timeSlices = activitySnapshotService.getAvailableTimeSlices(city);
        return ResponseEntity.ok(ApiResponse.success(timeSlices));
    }

    @GetMapping("/cities/{city}/latest")
    public ResponseEntity<ApiResponse<CityHotSnapshotDTO>> getLatestSnapshot(@PathVariable String city) {
        CityHotSnapshotDTO snapshot = activitySnapshotService.getLatestSnapshot(city);
        return ResponseEntity.ok(ApiResponse.success(snapshot));
    }

    @GetMapping("/cities/{city}/{timeSlice}")
    public ResponseEntity<ApiResponse<CityHotSnapshotDTO>> getSnapshotByTimeSlice(
            @PathVariable String city,
            @PathVariable String timeSlice) {
        CityHotSnapshotDTO snapshot = activitySnapshotService.getSnapshotByTimeSlice(city, timeSlice);
        return ResponseEntity.ok(ApiResponse.success(snapshot));
    }

    @GetMapping("/cities/{city}/range")
    public ResponseEntity<ApiResponse<List<CityHotSnapshotDTO>>> getSnapshotsByTimeRange(
            @PathVariable String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<CityHotSnapshotDTO> snapshots = activitySnapshotService.getSnapshotsByTimeRange(city, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(snapshots));
    }

    @GetMapping("/activities/{activityId}/trajectory")
    public ResponseEntity<ApiResponse<ActivityTrajectoryDTO>> getActivityTrajectory(
            @PathVariable Long activityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime) {
        ActivityTrajectoryDTO trajectory;
        if (startTime != null) {
            trajectory = activitySnapshotService.getActivityTrajectory(activityId, startTime);
        } else {
            trajectory = activitySnapshotService.getActivityTrajectory(activityId);
        }
        return ResponseEntity.ok(ApiResponse.success(trajectory));
    }

    @GetMapping("/cities/{city}/newly-entered")
    public ResponseEntity<ApiResponse<List<ActivityTrajectoryDTO>>> getNewlyEnteredActivities(
            @PathVariable String city,
            @RequestParam(defaultValue = "7") int days) {
        List<ActivityTrajectoryDTO> activities = activitySnapshotService.getNewlyEnteredActivities(city, days);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<String>> triggerSnapshot() {
        activitySnapshotService.createSnapshotForAllCities();
        return ResponseEntity.ok(ApiResponse.success("Snapshot triggered successfully"));
    }

    @PostMapping("/trigger/{city}")
    public ResponseEntity<ApiResponse<String>> triggerSnapshotForCity(@PathVariable String city) {
        activitySnapshotService.createSnapshotForCity(city);
        return ResponseEntity.ok(ApiResponse.success("Snapshot triggered for city: " + city));
    }
}
