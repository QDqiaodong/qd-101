package com.example.cityactivity.repository;

import com.example.cityactivity.entity.Registration;
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
    
    List<Registration> findByActivityIdAndCancelledFalse(Long activityId);
    
    @Modifying
    @Query("UPDATE Registration r SET r.cancelled = true WHERE r.activity.id = :activityId AND r.user.id = :userId")
    void cancelRegistration(@Param("activityId") Long activityId, @Param("userId") Long userId);
    
    long countByActivityIdAndCancelledFalse(Long activityId);
}
