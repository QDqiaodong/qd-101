package com.example.cityactivity.repository;

import com.example.cityactivity.entity.ActivityHotSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityHotSnapshotRepository extends JpaRepository<ActivityHotSnapshot, Long> {

    List<ActivityHotSnapshot> findByCityAndSnapshotTimeOrderByRankAsc(String city, LocalDateTime snapshotTime);

    List<ActivityHotSnapshot> findByCityAndTimeSliceOrderByRankAsc(String city, String timeSlice);

    @Query("SELECT s FROM ActivityHotSnapshot s WHERE s.city = :city AND s.snapshotTime >= :startTime AND s.snapshotTime <= :endTime ORDER BY s.snapshotTime DESC, s.rank ASC")
    List<ActivityHotSnapshot> findByCityAndTimeRange(
            @Param("city") String city,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM ActivityHotSnapshot s WHERE s.activityId = :activityId ORDER BY s.snapshotTime ASC")
    List<ActivityHotSnapshot> findByActivityIdOrderBySnapshotTimeAsc(@Param("activityId") Long activityId);

    @Query("SELECT s FROM ActivityHotSnapshot s WHERE s.activityId = :activityId AND s.snapshotTime >= :startTime ORDER BY s.snapshotTime ASC")
    List<ActivityHotSnapshot> findByActivityIdAndSnapshotTimeAfter(
            @Param("activityId") Long activityId,
            @Param("startTime") LocalDateTime startTime);

    @Query("SELECT DISTINCT s.city FROM ActivityHotSnapshot s")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT s.timeSlice FROM ActivityHotSnapshot s WHERE s.city = :city ORDER BY s.timeSlice DESC")
    List<String> findDistinctTimeSlicesByCity(@Param("city") String city);

    @Query("SELECT MIN(s.snapshotTime) FROM ActivityHotSnapshot s WHERE s.activityId = :activityId")
    LocalDateTime findFirstSnapshotTimeByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT MIN(s.rank) FROM ActivityHotSnapshot s WHERE s.activityId = :activityId")
    Integer findBestRankByActivityId(@Param("activityId") Long activityId);

    boolean existsByCityAndTimeSlice(String city, String timeSlice);
}
