package com.example.cityactivity.service.impl;

import com.example.cityactivity.dto.response.ActivityTrajectoryDTO;
import com.example.cityactivity.dto.response.CityHotSnapshotDTO;
import com.example.cityactivity.dto.response.HotSnapshotDTO;
import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.ActivityHotSnapshot;
import com.example.cityactivity.repository.ActivityHotSnapshotRepository;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.service.ActivitySnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivitySnapshotServiceImpl implements ActivitySnapshotService {

    private final ActivityRepository activityRepository;
    private final ActivityHotSnapshotRepository snapshotRepository;

    @Value("${activity.snapshot.top-n:20}")
    private int topN;

    @Value("${activity.snapshot.time-slice-format:yyyyMMddHH}")
    private String timeSliceFormat;

    @Override
    @Transactional
    public void createSnapshotForCity(String city) {
        LocalDateTime snapshotTime = LocalDateTime.now();
        String timeSlice = snapshotTime.format(DateTimeFormatter.ofPattern(timeSliceFormat));

        if (snapshotRepository.existsByCityAndTimeSlice(city, timeSlice)) {
            log.debug("Snapshot already exists for city {} at time slice {}, skipping", city, timeSlice);
            return;
        }

        List<Activity> activities = activityRepository.findByCityWithCreatorOrderByParticipantsDesc(city);

        List<ActivityHotSnapshot> snapshots = new ArrayList<>();
        int rank = 1;
        for (Activity activity : activities) {
            if (rank > topN) {
                break;
            }

            ActivityHotSnapshot snapshot = ActivityHotSnapshot.builder()
                    .city(city)
                    .activityId(activity.getId())
                    .activityTitle(activity.getTitle())
                    .activityType(activity.getType())
                    .activityImage(activity.getImage())
                    .rank(rank)
                    .currentParticipants(activity.getCurrentParticipants())
                    .views(activity.getViews())
                    .creatorId(activity.getCreator().getId())
                    .creatorName(activity.getCreator().getName())
                    .activityCreatedAt(activity.getCreatedAt())
                    .snapshotTime(snapshotTime)
                    .timeSlice(timeSlice)
                    .build();

            snapshots.add(snapshot);
            rank++;
        }

        snapshotRepository.saveAll(snapshots);
        log.info("Created hot snapshot for city {}: {} activities, time slice {}", city, snapshots.size(), timeSlice);
    }

    @Override
    @Transactional
    public void createSnapshotForAllCities() {
        List<String> cities = activityRepository.findDistinctCities();

        log.info("Creating hot snapshots for {} cities", cities.size());
        for (String city : cities) {
            try {
                createSnapshotForCity(city);
            } catch (Exception e) {
                log.error("Failed to create snapshot for city {}", city, e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CityHotSnapshotDTO getLatestSnapshot(String city) {
        List<String> timeSlices = snapshotRepository.findDistinctTimeSlicesByCity(city);
        if (timeSlices.isEmpty()) {
            return buildEmptySnapshot(city);
        }

        String latestTimeSlice = timeSlices.get(0);
        return getSnapshotByTimeSlice(city, latestTimeSlice);
    }

    @Override
    @Transactional(readOnly = true)
    public CityHotSnapshotDTO getSnapshotByTimeSlice(String city, String timeSlice) {
        List<ActivityHotSnapshot> snapshots = snapshotRepository.findByCityAndTimeSliceOrderByRankAsc(city, timeSlice);

        if (snapshots.isEmpty()) {
            return buildEmptySnapshot(city);
        }

        LocalDateTime snapshotTime = snapshots.get(0).getSnapshotTime();
        List<HotSnapshotDTO> rankings = snapshots.stream()
                .map(this::toHotSnapshotDTO)
                .collect(Collectors.toList());

        return CityHotSnapshotDTO.builder()
                .city(city)
                .timeSlice(timeSlice)
                .snapshotTime(snapshotTime)
                .rankings(rankings)
                .totalActivities(rankings.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityHotSnapshotDTO> getSnapshotsByTimeRange(String city, LocalDateTime startTime, LocalDateTime endTime) {
        List<ActivityHotSnapshot> snapshots = snapshotRepository.findByCityAndTimeRange(city, startTime, endTime);

        if (snapshots.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<ActivityHotSnapshot>> groupedByTimeSlice = snapshots.stream()
                .collect(Collectors.groupingBy(
                        ActivityHotSnapshot::getTimeSlice,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<CityHotSnapshotDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<ActivityHotSnapshot>> entry : groupedByTimeSlice.entrySet()) {
            String timeSlice = entry.getKey();
            List<ActivityHotSnapshot> sliceSnapshots = entry.getValue();
            sliceSnapshots.sort(Comparator.comparingInt(ActivityHotSnapshot::getRank));

            LocalDateTime snapshotTime = sliceSnapshots.get(0).getSnapshotTime();
            List<HotSnapshotDTO> rankings = sliceSnapshots.stream()
                    .map(this::toHotSnapshotDTO)
                    .collect(Collectors.toList());

            result.add(CityHotSnapshotDTO.builder()
                    .city(city)
                    .timeSlice(timeSlice)
                    .snapshotTime(snapshotTime)
                    .rankings(rankings)
                    .totalActivities(rankings.size())
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableCities() {
        return snapshotRepository.findDistinctCities();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableTimeSlices(String city) {
        return snapshotRepository.findDistinctTimeSlicesByCity(city);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTrajectoryDTO getActivityTrajectory(Long activityId) {
        List<ActivityHotSnapshot> snapshots = snapshotRepository.findByActivityIdOrderBySnapshotTimeAsc(activityId);
        return buildTrajectoryDTO(activityId, snapshots);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTrajectoryDTO getActivityTrajectory(Long activityId, LocalDateTime startTime) {
        List<ActivityHotSnapshot> snapshots = snapshotRepository.findByActivityIdAndSnapshotTimeAfter(activityId, startTime);
        return buildTrajectoryDTO(activityId, snapshots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityTrajectoryDTO> getNewlyEnteredActivities(String city, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<ActivityHotSnapshot> recentSnapshots = snapshotRepository.findByCityAndTimeRange(city, startTime, LocalDateTime.now());

        Set<Long> allActivityIds = recentSnapshots.stream()
                .map(ActivityHotSnapshot::getActivityId)
                .collect(Collectors.toSet());

        List<ActivityTrajectoryDTO> newlyEntered = new ArrayList<>();
        for (Long activityId : allActivityIds) {
            LocalDateTime firstSnapshotTime = snapshotRepository.findFirstSnapshotTimeByActivityId(activityId);
            if (firstSnapshotTime != null && firstSnapshotTime.isAfter(startTime)) {
                ActivityTrajectoryDTO trajectory = getActivityTrajectory(activityId);
                if (trajectory != null) {
                    newlyEntered.add(trajectory);
                }
            }
        }

        newlyEntered.sort(Comparator.comparing(t -> t.getFirstEnteredHotListAt()));
        return newlyEntered;
    }

    private ActivityTrajectoryDTO buildTrajectoryDTO(Long activityId, List<ActivityHotSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return null;
        }

        ActivityHotSnapshot first = snapshots.get(0);
        ActivityHotSnapshot last = snapshots.get(snapshots.size() - 1);

        int bestRank = Integer.MAX_VALUE;
        LocalDateTime bestRankAt = null;
        for (ActivityHotSnapshot snapshot : snapshots) {
            if (snapshot.getRank() < bestRank) {
                bestRank = snapshot.getRank();
                bestRankAt = snapshot.getSnapshotTime();
            }
        }

        List<ActivityTrajectoryDTO.TrajectoryPoint> trajectoryPoints = snapshots.stream()
                .map(s -> ActivityTrajectoryDTO.TrajectoryPoint.builder()
                        .snapshotTime(s.getSnapshotTime())
                        .timeSlice(s.getTimeSlice())
                        .rank(s.getRank())
                        .currentParticipants(s.getCurrentParticipants())
                        .views(s.getViews())
                        .build())
                .collect(Collectors.toList());

        return ActivityTrajectoryDTO.builder()
                .activityId(activityId)
                .activityTitle(first.getActivityTitle())
                .activityType(first.getActivityType())
                .city(first.getCity())
                .activityImage(first.getActivityImage())
                .activityCreatedAt(first.getActivityCreatedAt())
                .firstEnteredHotListAt(first.getSnapshotTime())
                .bestRank(bestRank)
                .bestRankAt(bestRankAt)
                .currentRank(last.getRank())
                .totalSnapshots(snapshots.size())
                .trajectoryPoints(trajectoryPoints)
                .build();
    }

    private HotSnapshotDTO toHotSnapshotDTO(ActivityHotSnapshot snapshot) {
        return HotSnapshotDTO.builder()
                .id(snapshot.getId())
                .activityId(snapshot.getActivityId())
                .activityTitle(snapshot.getActivityTitle())
                .activityType(snapshot.getActivityType())
                .activityImage(snapshot.getActivityImage())
                .rank(snapshot.getRank())
                .currentParticipants(snapshot.getCurrentParticipants())
                .views(snapshot.getViews())
                .creatorId(snapshot.getCreatorId())
                .creatorName(snapshot.getCreatorName())
                .activityCreatedAt(snapshot.getActivityCreatedAt())
                .snapshotTime(snapshot.getSnapshotTime())
                .timeSlice(snapshot.getTimeSlice())
                .build();
    }

    private CityHotSnapshotDTO buildEmptySnapshot(String city) {
        return CityHotSnapshotDTO.builder()
                .city(city)
                .rankings(Collections.emptyList())
                .totalActivities(0)
                .build();
    }
}
