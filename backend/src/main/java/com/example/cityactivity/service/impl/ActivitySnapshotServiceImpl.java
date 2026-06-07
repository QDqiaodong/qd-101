package com.example.cityactivity.service.impl;

import com.example.cityactivity.config.SnapshotPriorityProperties;
import com.example.cityactivity.dto.response.ActivityTrajectoryDTO;
import com.example.cityactivity.dto.response.CityActivityScoreDTO;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivitySnapshotServiceImpl implements ActivitySnapshotService {

    private final ActivityRepository activityRepository;
    private final ActivityHotSnapshotRepository snapshotRepository;
    private final SnapshotPriorityProperties priorityProperties;

    @Value("${activity.snapshot.top-n:20}")
    private int topN;

    @Value("${activity.snapshot.time-slice-format:yyyyMMddHHmm}")
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

    @Override
    @Transactional(readOnly = true)
    public List<CityActivityScoreDTO> calculateCityActivityScores() {
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> stats = activityRepository.getCityActivityStats(now);

        if (stats.isEmpty()) {
            return Collections.emptyList();
        }

        long maxParticipants = 1;
        long maxViews = 1;
        long maxActivities = 1;

        for (Object[] row : stats) {
            long activityCount = ((Number) row[1]).longValue();
            long totalParticipants = row[2] != null ? ((Number) row[2]).longValue() : 0;
            long totalViews = row[3] != null ? ((Number) row[3]).longValue() : 0;

            maxActivities = Math.max(maxActivities, activityCount);
            maxParticipants = Math.max(maxParticipants, totalParticipants);
            maxViews = Math.max(maxViews, totalViews);
        }

        List<CityActivityScoreDTO> scores = new ArrayList<>();
        for (Object[] row : stats) {
            String city = (String) row[0];
            long activityCount = ((Number) row[1]).longValue();
            long totalParticipants = row[2] != null ? ((Number) row[2]).longValue() : 0;
            long totalViews = row[3] != null ? ((Number) row[3]).longValue() : 0;

            double activityScore = (double) activityCount / maxActivities * 0.3;
            double participantScore = (double) totalParticipants / maxParticipants * 0.5;
            double viewScore = (double) totalViews / maxViews * 0.2;
            double totalScore = activityScore + participantScore + viewScore;

            scores.add(CityActivityScoreDTO.builder()
                    .city(city)
                    .activityCount(activityCount)
                    .totalParticipants(totalParticipants)
                    .totalViews(totalViews)
                    .score(totalScore)
                    .build());
        }

        scores.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        int totalCities = scores.size();
        int highTierCount = (int) Math.ceil(totalCities * priorityProperties.getHighTierRatio());
        int mediumTierCount = (int) Math.ceil(totalCities * priorityProperties.getMediumTierRatio());

        for (int i = 0; i < scores.size(); i++) {
            if (i < highTierCount) {
                scores.get(i).setPriorityTier(CityActivityScoreDTO.PriorityTier.HIGH);
            } else if (i < highTierCount + mediumTierCount) {
                scores.get(i).setPriorityTier(CityActivityScoreDTO.PriorityTier.MEDIUM);
            } else {
                scores.get(i).setPriorityTier(CityActivityScoreDTO.PriorityTier.LOW);
            }
        }

        return scores;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCitiesByPriorityTier(CityActivityScoreDTO.PriorityTier tier) {
        return calculateCityActivityScores().stream()
                .filter(score -> score.getPriorityTier() == tier)
                .map(CityActivityScoreDTO::getCity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCitiesWithSoonStartingActivities(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(hours);
        return activityRepository.findCitiesWithActivitiesStartingBetween(now, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> detectBurstCities() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookbackTime = now.minusMinutes(priorityProperties.getBurstLookbackMinutes());
        List<String> allCities = activityRepository.findDistinctCities();
        List<String> burstCities = new ArrayList<>();

        for (String city : allCities) {
            if (isBurstCity(city, lookbackTime, now)) {
                burstCities.add(city);
            }
        }

        return burstCities;
    }

    private boolean isBurstCity(String city, LocalDateTime lookbackTime, LocalDateTime now) {
        List<ActivityHotSnapshot> recentSnapshots = snapshotRepository.findLatestByCityAndTimeAfter(city, lookbackTime);

        if (recentSnapshots.size() < 2) {
            return false;
        }

        Map<Long, Integer> earliestParticipants = new HashMap<>();
        Map<Long, Integer> latestParticipants = new HashMap<>();

        for (ActivityHotSnapshot snapshot : recentSnapshots) {
            Long activityId = snapshot.getActivityId();
            int participants = snapshot.getCurrentParticipants();

            earliestParticipants.putIfAbsent(activityId, participants);
            latestParticipants.put(activityId, participants);
        }

        int totalGrowth = 0;
        int totalBase = 0;

        for (Map.Entry<Long, Integer> entry : earliestParticipants.entrySet()) {
            Long activityId = entry.getKey();
            int base = entry.getValue();
            int current = latestParticipants.getOrDefault(activityId, base);
            int growth = current - base;

            totalGrowth += growth;
            totalBase += base;
        }

        if (totalBase == 0) {
            return false;
        }

        double growthRate = (double) totalGrowth / totalBase;
        return growthRate >= priorityProperties.getBurstGrowthThreshold();
    }

    @Override
    @Transactional
    public void createPrioritySnapshots() {
        List<CityActivityScoreDTO> scores = calculateCityActivityScores();

        List<String> criticalStartingCities = getCitiesWithSoonStartingActivities(
                priorityProperties.getCriticalStartingHours());
        List<String> burstCities = detectBurstCities();

        Set<String> citiesToRefresh = new LinkedHashSet<>();

        for (CityActivityScoreDTO score : scores) {
            String city = score.getCity();
            if (needsRefresh(city, score.getPriorityTier())) {
                citiesToRefresh.add(city);
            }
        }

        for (String city : criticalStartingCities) {
            if (needsRefreshForCriticalStart(city)) {
                citiesToRefresh.add(city);
            }
        }

        for (String city : burstCities) {
            citiesToRefresh.add(city);
        }

        log.info("Priority snapshot refresh: {} cities scheduled (tier-based + {} critical-starting + {} burst)",
                citiesToRefresh.size(), criticalStartingCities.size(), burstCities.size());

        for (String city : citiesToRefresh) {
            try {
                createSnapshotForCity(city);
            } catch (Exception e) {
                log.error("Failed to create priority snapshot for city {}", city, e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean needsRefresh(String city) {
        List<CityActivityScoreDTO> scores = calculateCityActivityScores();
        CityActivityScoreDTO.PriorityTier tier = scores.stream()
                .filter(s -> s.getCity().equals(city))
                .map(CityActivityScoreDTO::getPriorityTier)
                .findFirst()
                .orElse(CityActivityScoreDTO.PriorityTier.LOW);
        return needsRefresh(city, tier);
    }

    private boolean needsRefresh(String city, CityActivityScoreDTO.PriorityTier tier) {
        LocalDateTime lastSnapshotTime = snapshotRepository.findLatestSnapshotTimeByCity(city);

        if (lastSnapshotTime == null) {
            return true;
        }

        int intervalMinutes = switch (tier) {
            case HIGH -> priorityProperties.getHighTierIntervalMinutes();
            case MEDIUM -> priorityProperties.getMediumTierIntervalMinutes();
            case LOW -> priorityProperties.getLowTierIntervalMinutes();
        };

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastSnapshot = ChronoUnit.MINUTES.between(lastSnapshotTime, now);

        return minutesSinceLastSnapshot >= intervalMinutes;
    }

    private boolean needsRefreshForCriticalStart(String city) {
        LocalDateTime lastSnapshotTime = snapshotRepository.findLatestSnapshotTimeByCity(city);

        if (lastSnapshotTime == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastSnapshot = ChronoUnit.MINUTES.between(lastSnapshotTime, now);

        return minutesSinceLastSnapshot >= 10;
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
