package com.example.cityactivity.repository;

import com.example.cityactivity.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    List<Activity> findByCity(String city);
    
    List<Activity> findByType(String type);
    
    List<Activity> findByCityAndType(String city, String type);
    
    List<Activity> findByCreatorId(Long creatorId);
    
    @Query("SELECT a FROM Activity a ORDER BY a.createdAt DESC")
    List<Activity> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT a FROM Activity a ORDER BY a.views DESC")
    List<Activity> findAllOrderByViewsDesc();
    
    @Query("SELECT a FROM Activity a ORDER BY a.currentParticipants DESC")
    List<Activity> findAllOrderByParticipantsDesc();
    
    @Modifying
    @Query("UPDATE Activity a SET a.views = a.views + 1 WHERE a.id = :id")
    void incrementViews(@Param("id") Long id);
    
    @Modifying
    @Query("UPDATE Activity a SET a.currentParticipants = a.currentParticipants + 1 WHERE a.id = :id")
    void incrementParticipants(@Param("id") Long id);
    
    @Modifying
    @Query("UPDATE Activity a SET a.currentParticipants = a.currentParticipants - 1 WHERE a.id = :id")
    void decrementParticipants(@Param("id") Long id);
    
    @Query("SELECT a FROM Activity a WHERE a.createdAt >= :startTime ORDER BY a.currentParticipants DESC")
    List<Activity> findHotActivitiesSince(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT a FROM Activity a WHERE a.createdAt >= :startTime ORDER BY a.views DESC")
    List<Activity> findPopularActivitiesSince(@Param("startTime") LocalDateTime startTime);
    
    long countByCreatorIdAndCreatedAtAfter(Long creatorId, LocalDateTime startTime);
    
    Optional<Activity> findTopByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    
    List<Activity> findByCreatorIdAndCreatedAtAfter(Long creatorId, LocalDateTime startTime);
    
    @Query("SELECT a FROM Activity a WHERE a.creator.id = :creatorId AND a.type = :type AND a.city = :city AND a.location = :location AND a.createdAt >= :startTime")
    List<Activity> findSimilarActivities(
            @Param("creatorId") Long creatorId,
            @Param("type") String type,
            @Param("city") String city,
            @Param("location") String location,
            @Param("startTime") LocalDateTime startTime);

    @Query("SELECT a FROM Activity a JOIN FETCH a.creator WHERE a.city = :city ORDER BY a.currentParticipants DESC")
    List<Activity> findByCityWithCreatorOrderByParticipantsDesc(@Param("city") String city);

    @Query("SELECT DISTINCT a.city FROM Activity a")
    List<String> findDistinctCities();
}
