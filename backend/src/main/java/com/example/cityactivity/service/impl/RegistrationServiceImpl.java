package com.example.cityactivity.service.impl;

import com.example.cityactivity.dto.request.RegistrationRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.AttendanceStatsDTO;
import com.example.cityactivity.dto.response.RegistrationStatusDTO;
import com.example.cityactivity.dto.response.RegistrationUserResponse;
import com.example.cityactivity.dto.response.WaitlistUserResponse;
import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.AttendanceStatus;
import com.example.cityactivity.entity.Registration;
import com.example.cityactivity.entity.RegistrationStatus;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.exception.BusinessException;
import com.example.cityactivity.exception.ResourceNotFoundException;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.repository.RegistrationRepository;
import com.example.cityactivity.repository.UserRepository;
import com.example.cityactivity.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {
    
    private final RegistrationRepository registrationRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = {"activities", "hot_activities", "activity_detail", "user_registrations"}, allEntries = true)
    public void register(RegistrationRequest request) {
        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity", request.getActivityId()));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));
        
        Optional<Registration> existingReg = registrationRepository.findByActivityIdAndUserIdAndStatus(
                request.getActivityId(), request.getUserId(), RegistrationStatus.CONFIRMED);
        if (existingReg.isPresent()) {
            throw new BusinessException("您已报名该活动");
        }
        
        Optional<Registration> existingWaitlist = registrationRepository.findByActivityIdAndUserIdAndStatus(
                request.getActivityId(), request.getUserId(), RegistrationStatus.WAITLISTED);
        if (existingWaitlist.isPresent()) {
            throw new BusinessException("您已在候补队列中");
        }
        
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            Integer maxPosition = registrationRepository.findMaxWaitlistPositionByActivityIdAndStatus(
                    request.getActivityId(), RegistrationStatus.WAITLISTED);
            int nextPosition = (maxPosition != null ? maxPosition : 0) + 1;
            
            Registration waitlistReg = Registration.builder()
                    .activity(activity)
                    .user(user)
                    .registeredAt(LocalDateTime.now())
                    .cancelled(false)
                    .status(RegistrationStatus.WAITLISTED)
                    .waitlistPosition(nextPosition)
                    .build();
            
            registrationRepository.save(waitlistReg);
            log.info("User {} added to waitlist for activity {} at position {}", 
                    request.getUserId(), request.getActivityId(), nextPosition);
            return;
        }
        
        Registration registration = Registration.builder()
                .activity(activity)
                .user(user)
                .registeredAt(LocalDateTime.now())
                .cancelled(false)
                .status(RegistrationStatus.CONFIRMED)
                .build();
        
        registrationRepository.save(registration);
        activityRepository.incrementParticipants(request.getActivityId());
        
        log.info("User {} registered for activity {}", request.getUserId(), request.getActivityId());
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"activities", "hot_activities", "activity_detail", "user_registrations"}, allEntries = true)
    public void cancelRegistration(Long activityId, Long userId) {
        Optional<Registration> confirmedReg = registrationRepository.findByActivityIdAndUserIdAndStatus(
                activityId, userId, RegistrationStatus.CONFIRMED);
        
        if (confirmedReg.isPresent()) {
            cancelConfirmedRegistration(confirmedReg.get());
            return;
        }
        
        Optional<Registration> waitlistReg = registrationRepository.findByActivityIdAndUserIdAndStatus(
                activityId, userId, RegistrationStatus.WAITLISTED);
        
        if (waitlistReg.isPresent()) {
            cancelWaitlistRegistration(waitlistReg.get());
            return;
        }
        
        throw new BusinessException("未找到报名记录");
    }
    
    private void cancelConfirmedRegistration(Registration registration) {
        registration.setCancelled(true);
        registration.setCancelledAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);
        
        activityRepository.decrementParticipants(registration.getActivity().getId());
        
        log.info("User {} cancelled registration for activity {}", 
                registration.getUser().getId(), registration.getActivity().getId());
        
        promoteNextWaitlistUser(registration.getActivity().getId());
    }
    
    private void cancelWaitlistRegistration(Registration registration) {
        Integer cancelledPosition = registration.getWaitlistPosition();
        
        registration.setCancelled(true);
        registration.setCancelledAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setWaitlistPosition(null);
        registrationRepository.save(registration);
        
        if (cancelledPosition != null) {
            registrationRepository.decrementWaitlistPositionsAfter(
                    registration.getActivity().getId(), 
                    RegistrationStatus.WAITLISTED, 
                    cancelledPosition);
        }
        
        log.info("User {} removed from waitlist for activity {}", 
                registration.getUser().getId(), registration.getActivity().getId());
    }
    
    private void promoteNextWaitlistUser(Long activityId) {
        Optional<Registration> nextWaitlist = registrationRepository
                .findFirstByActivityIdAndStatusOrderByWaitlistPositionAsc(activityId, RegistrationStatus.WAITLISTED);
        
        if (nextWaitlist.isEmpty()) {
            log.info("No waitlist users to promote for activity {}", activityId);
            return;
        }
        
        Registration promoted = nextWaitlist.get();
        Integer promotedPosition = promoted.getWaitlistPosition();
        
        promoted.setStatus(RegistrationStatus.CONFIRMED);
        promoted.setWaitlistPosition(null);
        registrationRepository.save(promoted);
        
        activityRepository.incrementParticipants(activityId);
        
        if (promotedPosition != null) {
            registrationRepository.decrementWaitlistPositionsAfter(
                    activityId, RegistrationStatus.WAITLISTED, promotedPosition);
        }
        
        log.info("User {} promoted from waitlist position {} for activity {}", 
                promoted.getUser().getId(), promotedPosition, activityId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isRegistered(Long activityId, Long userId) {
        return registrationRepository.findByActivityIdAndUserIdAndStatus(
                activityId, userId, RegistrationStatus.CONFIRMED).isPresent();
    }
    
    @Override
    @Transactional(readOnly = true)
    public RegistrationStatusDTO getRegistrationStatus(Long activityId, Long userId) {
        Optional<Registration> confirmed = registrationRepository.findByActivityIdAndUserIdAndStatus(
                activityId, userId, RegistrationStatus.CONFIRMED);
        if (confirmed.isPresent()) {
            return RegistrationStatusDTO.CONFIRMED;
        }
        
        Optional<Registration> waitlisted = registrationRepository.findByActivityIdAndUserIdAndStatus(
                activityId, userId, RegistrationStatus.WAITLISTED);
        if (waitlisted.isPresent()) {
            return RegistrationStatusDTO.WAITLISTED;
        }
        
        Optional<Registration> cancelled = registrationRepository.findByActivityIdAndUserIdAndStatus(
                activityId, userId, RegistrationStatus.CANCELLED);
        if (cancelled.isPresent()) {
            return RegistrationStatusDTO.CANCELLED;
        }
        
        return RegistrationStatusDTO.NOT_REGISTERED;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getWaitlistPosition(Long activityId, Long userId) {
        Optional<Registration> reg = registrationRepository.findByActivityIdAndUserIdAndStatus(
                activityId, userId, RegistrationStatus.WAITLISTED);
        return reg.map(Registration::getWaitlistPosition).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WaitlistUserResponse> getWaitlist(Long activityId) {
        List<Registration> waitlist = registrationRepository
                .findByActivityIdAndStatusOrderByWaitlistPositionAsc(activityId, RegistrationStatus.WAITLISTED);
        
        return waitlist.stream()
                .map(r -> WaitlistUserResponse.builder()
                        .userId(r.getUser().getId())
                        .userName(r.getUser().getName())
                        .waitlistPosition(r.getWaitlistPosition())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getWaitlistCount(Long activityId) {
        return (int) registrationRepository.countByActivityIdAndStatus(activityId, RegistrationStatus.WAITLISTED);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_registrations", key = "'user:' + #userId")
    public List<ActivityResponse> getRegisteredActivities(Long userId) {
        List<Registration> registrations = registrationRepository.findByUserIdAndCancelledFalse(userId);
        
        return registrations.stream()
                .map(r -> {
                    Activity a = r.getActivity();
                    Integer waitlistCount = (int) registrationRepository.countByActivityIdAndStatus(
                            a.getId(), RegistrationStatus.WAITLISTED);
                    
                    long attendanceConfirmed = registrationRepository.countByActivityIdAndStatusAndAttendanceStatus(
                            a.getId(), RegistrationStatus.CONFIRMED, AttendanceStatus.CONFIRMED);
                    long attendancePending = registrationRepository.countByActivityIdAndStatusAndAttendanceStatus(
                            a.getId(), RegistrationStatus.CONFIRMED, AttendanceStatus.PENDING);
                    long attendanceDeclined = registrationRepository.countByActivityIdAndStatusAndAttendanceStatus(
                            a.getId(), RegistrationStatus.CONFIRMED, AttendanceStatus.DECLINED);
                    
                    AttendanceStatsDTO attendanceStats = AttendanceStatsDTO.builder()
                            .totalConfirmed(a.getCurrentParticipants())
                            .attendanceConfirmed((int) attendanceConfirmed)
                            .attendancePending((int) attendancePending)
                            .attendanceDeclined((int) attendanceDeclined)
                            .build();
                    
                    return ActivityResponse.builder()
                            .id(a.getId())
                            .title(a.getTitle())
                            .type(a.getType())
                            .city(a.getCity())
                            .location(a.getLocation())
                            .time(a.getTime())
                            .maxParticipants(a.getMaxParticipants())
                            .currentParticipants(a.getCurrentParticipants())
                            .description(a.getDescription())
                            .requirements(a.getRequirements())
                            .image(a.getImage())
                            .views(a.getViews())
                            .createdAt(a.getCreatedAt())
                            .creatorId(a.getCreator().getId())
                            .creatorName(a.getCreator().getName())
                            .waitlistCount(waitlistCount)
                            .attendanceStats(attendanceStats)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"activities", "hot_activities", "activity_detail", "user_registrations"}, allEntries = true)
    public void confirmAttendance(Long activityId, Long userId, AttendanceStatus status) {
        Optional<Registration> registrationOpt = registrationRepository.findByActivityIdAndUserIdAndStatus(
                activityId, userId, RegistrationStatus.CONFIRMED);
        
        if (registrationOpt.isEmpty()) {
            registrationOpt = registrationRepository.findByActivityIdAndUserIdAndStatus(
                    activityId, userId, RegistrationStatus.WAITLISTED);
        }
        
        if (registrationOpt.isEmpty()) {
            throw new BusinessException("未找到报名记录");
        }
        
        Registration registration = registrationOpt.get();
        registration.setAttendanceStatus(status);
        registration.setAttendanceConfirmedAt(LocalDateTime.now());
        registrationRepository.save(registration);
        
        log.info("User {} updated attendance status to {} for activity {}", 
                userId, status, activityId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AttendanceStatsDTO getAttendanceStats(Long activityId) {
        long confirmed = registrationRepository.countByActivityIdAndStatusAndAttendanceStatus(
                activityId, RegistrationStatus.CONFIRMED, AttendanceStatus.CONFIRMED);
        long pending = registrationRepository.countByActivityIdAndStatusAndAttendanceStatus(
                activityId, RegistrationStatus.CONFIRMED, AttendanceStatus.PENDING);
        long declined = registrationRepository.countByActivityIdAndStatusAndAttendanceStatus(
                activityId, RegistrationStatus.CONFIRMED, AttendanceStatus.DECLINED);
        
        long totalConfirmed = registrationRepository.countByActivityIdAndStatus(
                activityId, RegistrationStatus.CONFIRMED);
        
        return AttendanceStatsDTO.builder()
                .totalConfirmed((int) totalConfirmed)
                .attendanceConfirmed((int) confirmed)
                .attendancePending((int) pending)
                .attendanceDeclined((int) declined)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RegistrationUserResponse> getConfirmedRegistrations(Long activityId) {
        List<Registration> registrations = registrationRepository
                .findByActivityIdAndStatusOrderByWaitlistPositionAsc(activityId, RegistrationStatus.CONFIRMED);
        
        return registrations.stream()
                .map(r -> RegistrationUserResponse.builder()
                        .userId(r.getUser().getId())
                        .userName(r.getUser().getName())
                        .userAvatar(r.getUser().getAvatar())
                        .registeredAt(r.getRegisteredAt())
                        .attendanceStatus(r.getAttendanceStatus())
                        .attendanceConfirmedAt(r.getAttendanceConfirmedAt())
                        .waitlistPosition(r.getWaitlistPosition())
                        .build())
                .collect(Collectors.toList());
    }
}
