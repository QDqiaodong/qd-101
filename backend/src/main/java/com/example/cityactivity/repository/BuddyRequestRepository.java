package com.example.cityactivity.repository;

import com.example.cityactivity.entity.BuddyRequest;
import com.example.cityactivity.enums.BuddyRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuddyRequestRepository extends JpaRepository<BuddyRequest, Long> {

    List<BuddyRequest> findByCity(String city);

    List<BuddyRequest> findByType(String type);

    List<BuddyRequest> findByCityAndType(String city, String type);

    List<BuddyRequest> findByCreatorId(Long creatorId);

    List<BuddyRequest> findByStatus(BuddyRequestStatus status);

    List<BuddyRequest> findByCityAndStatus(String city, BuddyRequestStatus status);

    List<BuddyRequest> findByTypeAndStatus(String type, BuddyRequestStatus status);

    List<BuddyRequest> findByCityAndTypeAndStatus(String city, String type, BuddyRequestStatus status);

    @Query("SELECT b FROM BuddyRequest b WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<BuddyRequest> findAllByStatusOrderByCreatedAtDesc(@Param("status") BuddyRequestStatus status);

    @Query("SELECT b FROM BuddyRequest b WHERE b.city = :city AND b.status = :status ORDER BY b.createdAt DESC")
    List<BuddyRequest> findByCityAndStatusOrderByCreatedAtDesc(@Param("city") String city, @Param("status") BuddyRequestStatus status);

    @Query("SELECT b FROM BuddyRequest b WHERE b.type = :type AND b.status = :status ORDER BY b.createdAt DESC")
    List<BuddyRequest> findByTypeAndStatusOrderByCreatedAtDesc(@Param("type") String type, @Param("status") BuddyRequestStatus status);

    @Query("SELECT b FROM BuddyRequest b WHERE b.city = :city AND b.type = :type AND b.status = :status ORDER BY b.createdAt DESC")
    List<BuddyRequest> findByCityAndTypeAndStatusOrderByCreatedAtDesc(@Param("city") String city, @Param("type") String type, @Param("status") BuddyRequestStatus status);

    @Query("SELECT b FROM BuddyRequest b WHERE b.city = :city AND b.type = :type AND b.status = 'OPEN' AND b.creator.id != :excludeUserId ORDER BY b.createdAt DESC")
    List<BuddyRequest> findMatchingRequests(@Param("city") String city, @Param("type") String type, @Param("excludeUserId") Long excludeUserId);
}
