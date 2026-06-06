package com.example.cityactivity.repository;

import com.example.cityactivity.entity.Registration;
import com.example.cityactivity.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    
    Optional<Registration> findByActivityIdAndUserIdAndCancelledFalse(Long activityId, Long userId);
    
    List<Registration> findByUserIdAndCancelledFalse(Long userId);
    
    List<Registration> findByUserId(Long userId);
    
    List<Registration> findByActivityIdAndCancelledFalse(Long activityId);
    
    @Modifying
    @Query("UPDATE Registration r SET r.cancelled = true WHERE r.activity.id = :activityId AND r.user.id = :userId")
    void cancelRegistration(@Param("activityId") Long activityId, @Param("userId") Long userId);
    
    long countByActivityIdAndCancelledFalse(Long activityId);
    
    Optional<Registration> findByActivityIdAndUserIdAndStatus(Long activityId, Long userId, RegistrationStatus status);
    
    Optional<Registration> findFirstByActivityIdAndStatusOrderByWaitlistPositionAsc(Long activityId, RegistrationStatus status);
    
    List<Registration> findByActivityIdAndStatusOrderByWaitlistPositionAsc(Long activityId, RegistrationStatus status);
    
    long countByActivityIdAndStatus(Long activityId, RegistrationStatus status);
    
    @Query("SELECT COALESCE(MAX(r.waitlistPosition), 0) FROM Registration r WHERE r.activity.id = :activityId AND r.status = :status")
    Integer findMaxWaitlistPositionByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") RegistrationStatus status);
    
    @Modifying
    @Query("UPDATE Registration r SET r.waitlistPosition = r.waitlistPosition - 1 WHERE r.activity.id = :activityId AND r.status = :status AND r.waitlistPosition > :position")
    void decrementWaitlistPositionsAfter(@Param("activityId") Long activityId, @Param("status") RegistrationStatus status, @Param("position") Integer position);
}
