package com.example.cityactivity.repository;

import com.example.cityactivity.entity.BuddyApplication;
import com.example.cityactivity.enums.BuddyApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuddyApplicationRepository extends JpaRepository<BuddyApplication, Long> {

    List<BuddyApplication> findByBuddyRequestId(Long requestId);

    List<BuddyApplication> findByBuddyRequestIdAndStatus(Long requestId, BuddyApplicationStatus status);

    List<BuddyApplication> findByApplicantId(Long applicantId);

    List<BuddyApplication> findByApplicantIdAndStatus(Long applicantId, BuddyApplicationStatus status);

    Optional<BuddyApplication> findByBuddyRequestIdAndApplicantId(Long requestId, Long applicantId);

    long countByBuddyRequestIdAndStatus(Long requestId, BuddyApplicationStatus status);

    boolean existsByBuddyRequestIdAndApplicantId(Long requestId, Long applicantId);
}
