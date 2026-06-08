package com.example.cityactivity.service.impl;

import com.example.cityactivity.dto.request.ActivityCreateRequest;
import com.example.cityactivity.dto.request.BuddyApplicationRequest;
import com.example.cityactivity.dto.request.BuddyConvertActivityRequest;
import com.example.cityactivity.dto.request.BuddyRequestCreateRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.BuddyApplicationResponse;
import com.example.cityactivity.dto.response.BuddyRequestResponse;
import com.example.cityactivity.entity.BuddyApplication;
import com.example.cityactivity.entity.BuddyRequest;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.enums.BuddyApplicationStatus;
import com.example.cityactivity.enums.BuddyRequestStatus;
import com.example.cityactivity.exception.BusinessException;
import com.example.cityactivity.exception.ResourceNotFoundException;
import com.example.cityactivity.repository.BuddyApplicationRepository;
import com.example.cityactivity.repository.BuddyRequestRepository;
import com.example.cityactivity.service.ActivityService;
import com.example.cityactivity.service.BuddyService;
import com.example.cityactivity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuddyServiceImpl implements BuddyService {

    private final BuddyRequestRepository buddyRequestRepository;
    private final BuddyApplicationRepository buddyApplicationRepository;
    private final UserService userService;
    private final ActivityService activityService;

    @Override
    @Transactional
    public BuddyRequestResponse createRequest(BuddyRequestCreateRequest request) {
        User creator = userService.findById(request.getCreatorId());

        BuddyRequest buddyRequest = BuddyRequest.builder()
                .title(request.getTitle())
                .type(request.getType())
                .city(request.getCity())
                .description(request.getDescription())
                .targetCount(request.getTargetCount() != null ? request.getTargetCount() : 1)
                .currentCount(1)
                .status(BuddyRequestStatus.OPEN)
                .creator(creator)
                .build();

        BuddyRequest saved = buddyRequestRepository.save(buddyRequest);
        log.info("Created buddy request: {}", saved.getId());
        return toRequestResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BuddyRequestResponse getRequestById(Long id) {
        BuddyRequest request = buddyRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuddyRequest", id));
        return toRequestResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuddyRequestResponse> getAllRequests(String city, String type, String status, String sortBy) {
        BuddyRequestStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = BuddyRequestStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的状态值: " + status);
            }
        }

        List<BuddyRequest> requests;

        if (city != null && type != null && statusEnum != null) {
            requests = buddyRequestRepository.findByCityAndTypeAndStatusOrderByCreatedAtDesc(city, type, statusEnum);
        } else if (city != null && type != null) {
            requests = buddyRequestRepository.findByCityAndType(city, type);
        } else if (city != null && statusEnum != null) {
            requests = buddyRequestRepository.findByCityAndStatusOrderByCreatedAtDesc(city, statusEnum);
        } else if (type != null && statusEnum != null) {
            requests = buddyRequestRepository.findByTypeAndStatusOrderByCreatedAtDesc(type, statusEnum);
        } else if (city != null) {
            requests = buddyRequestRepository.findByCity(city);
        } else if (type != null) {
            requests = buddyRequestRepository.findByType(type);
        } else if (statusEnum != null) {
            requests = buddyRequestRepository.findAllByStatusOrderByCreatedAtDesc(statusEnum);
        } else {
            requests = buddyRequestRepository.findAll();
        }

        sortBuddyRequests(requests, sortBy);
        return requests.stream().map(this::toRequestResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuddyRequestResponse> getRequestsByCreator(Long creatorId) {
        List<BuddyRequest> requests = buddyRequestRepository.findByCreatorId(creatorId);
        requests.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return requests.stream().map(this::toRequestResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuddyRequestResponse> getMatchRecommendations(Long userId, String city) {
        List<BuddyRequest> userRequests = buddyRequestRepository.findByCreatorId(userId);
        if (userRequests.isEmpty()) {
            return getDefaultRecommendations(userId, city);
        }

        List<BuddyRequest> allMatches = new ArrayList<>();
        for (BuddyRequest userRequest : userRequests) {
            if (userRequest.getStatus() != BuddyRequestStatus.OPEN
                    && userRequest.getStatus() != BuddyRequestStatus.MATCHING) {
                continue;
            }
            List<BuddyRequest> matches = buddyRequestRepository.findMatchingRequests(
                    userRequest.getCity(),
                    userRequest.getType(),
                    userId
            );
            allMatches.addAll(matches);
        }

        List<BuddyRequest> uniqueMatches = allMatches.stream()
                .distinct()
                .sorted(Comparator.comparing(BuddyRequest::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());

        if (uniqueMatches.isEmpty()) {
            return getDefaultRecommendations(userId, city);
        }

        return uniqueMatches.stream().map(this::toRequestResponse).collect(Collectors.toList());
    }

    private List<BuddyRequestResponse> getDefaultRecommendations(Long userId, String city) {
        List<BuddyRequest> requests;
        if (city != null && !city.isEmpty()) {
            requests = buddyRequestRepository.findByCityAndStatusOrderByCreatedAtDesc(city, BuddyRequestStatus.OPEN);
        } else {
            requests = buddyRequestRepository.findAllByStatusOrderByCreatedAtDesc(BuddyRequestStatus.OPEN);
        }

        return requests.stream()
                .filter(r -> !r.getCreator().getId().equals(userId))
                .limit(10)
                .map(this::toRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BuddyApplicationResponse applyForBuddy(BuddyApplicationRequest request) {
        BuddyRequest buddyRequest = buddyRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("BuddyRequest", request.getRequestId()));

        if (buddyRequest.getStatus() != BuddyRequestStatus.OPEN
                && buddyRequest.getStatus() != BuddyRequestStatus.MATCHING) {
            throw new BusinessException("该搭子征集已关闭或已完成配对");
        }

        if (buddyRequest.getCreator().getId().equals(request.getApplicantId())) {
            throw new BusinessException("不能申请自己发布的搭子征集");
        }

        if (buddyApplicationRepository.existsByBuddyRequestIdAndApplicantId(
                request.getRequestId(), request.getApplicantId())) {
            throw new BusinessException("您已经申请过该搭子征集了");
        }

        User applicant = userService.findById(request.getApplicantId());

        BuddyApplication application = BuddyApplication.builder()
                .buddyRequest(buddyRequest)
                .applicant(applicant)
                .message(request.getMessage())
                .status(BuddyApplicationStatus.PENDING)
                .build();

        BuddyApplication saved = buddyApplicationRepository.save(application);
        log.info("Created buddy application: {} for request: {}", saved.getId(), request.getRequestId());

        if (buddyRequest.getStatus() == BuddyRequestStatus.OPEN) {
            buddyRequest.setStatus(BuddyRequestStatus.MATCHING);
            buddyRequestRepository.save(buddyRequest);
        }

        return toApplicationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuddyApplicationResponse> getApplicationsByRequest(Long requestId) {
        List<BuddyApplication> applications = buddyApplicationRepository.findByBuddyRequestId(requestId);
        applications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return applications.stream().map(this::toApplicationResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuddyApplicationResponse> getApplicationsByApplicant(Long applicantId) {
        List<BuddyApplication> applications = buddyApplicationRepository.findByApplicantId(applicantId);
        applications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return applications.stream().map(this::toApplicationResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BuddyApplicationResponse acceptApplication(Long applicationId, Long creatorId) {
        BuddyApplication application = buddyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("BuddyApplication", applicationId));

        BuddyRequest request = application.getBuddyRequest();
        if (!request.getCreator().getId().equals(creatorId)) {
            throw new BusinessException("无权限操作该申请");
        }

        if (application.getStatus() != BuddyApplicationStatus.PENDING) {
            throw new BusinessException("该申请已处理过了");
        }

        if (request.getCurrentCount() >= request.getTargetCount()) {
            throw new BusinessException("已达到目标人数，无法再接受申请");
        }

        application.setStatus(BuddyApplicationStatus.ACCEPTED);
        BuddyApplication saved = buddyApplicationRepository.save(application);

        request.setCurrentCount(request.getCurrentCount() + 1);
        if (request.getCurrentCount() >= request.getTargetCount()) {
            request.setStatus(BuddyRequestStatus.MATCHED);
        }
        buddyRequestRepository.save(request);

        log.info("Accepted buddy application: {} for request: {}", applicationId, request.getId());
        return toApplicationResponse(saved);
    }

    @Override
    @Transactional
    public BuddyApplicationResponse rejectApplication(Long applicationId, Long creatorId, String reason) {
        BuddyApplication application = buddyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("BuddyApplication", applicationId));

        BuddyRequest request = application.getBuddyRequest();
        if (!request.getCreator().getId().equals(creatorId)) {
            throw new BusinessException("无权限操作该申请");
        }

        if (application.getStatus() != BuddyApplicationStatus.PENDING) {
            throw new BusinessException("该申请已处理过了");
        }

        application.setStatus(BuddyApplicationStatus.REJECTED);
        BuddyApplication saved = buddyApplicationRepository.save(application);

        log.info("Rejected buddy application: {} for request: {} reason: {}", applicationId, request.getId(), reason);
        return toApplicationResponse(saved);
    }

    @Override
    @Transactional
    public BuddyApplicationResponse cancelApplication(Long applicationId, Long applicantId) {
        BuddyApplication application = buddyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("BuddyApplication", applicationId));

        if (!application.getApplicant().getId().equals(applicantId)) {
            throw new BusinessException("无权限取消该申请");
        }

        if (application.getStatus() == BuddyApplicationStatus.CANCELLED) {
            throw new BusinessException("该申请已取消");
        }

        BuddyRequest request = application.getBuddyRequest();

        if (application.getStatus() == BuddyApplicationStatus.ACCEPTED) {
            request.setCurrentCount(request.getCurrentCount() - 1);
            if (request.getStatus() == BuddyRequestStatus.MATCHED) {
                request.setStatus(BuddyRequestStatus.MATCHING);
            }
        }

        application.setStatus(BuddyApplicationStatus.CANCELLED);
        BuddyApplication saved = buddyApplicationRepository.save(application);
        buddyRequestRepository.save(request);

        log.info("Cancelled buddy application: {} for request: {}", applicationId, request.getId());
        return toApplicationResponse(saved);
    }

    @Override
    @Transactional
    public BuddyRequestResponse closeRequest(Long requestId, Long creatorId) {
        BuddyRequest request = buddyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BuddyRequest", requestId));

        if (!request.getCreator().getId().equals(creatorId)) {
            throw new BusinessException("无权限关闭该征集");
        }

        if (request.getStatus() == BuddyRequestStatus.CLOSED || request.getStatus() == BuddyRequestStatus.CONVERTED) {
            throw new BusinessException("该征集已关闭或已转换为活动");
        }

        request.setStatus(BuddyRequestStatus.CLOSED);
        BuddyRequest saved = buddyRequestRepository.save(request);

        log.info("Closed buddy request: {}", requestId);
        return toRequestResponse(saved);
    }

    @Override
    @Transactional
    public ActivityResponse convertToActivity(BuddyConvertActivityRequest request) {
        BuddyRequest buddyRequest = buddyRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("BuddyRequest", request.getRequestId()));

        if (!buddyRequest.getCreator().getId().equals(request.getCreatorId())) {
            throw new BusinessException("无权限操作该征集");
        }

        if (buddyRequest.getStatus() == BuddyRequestStatus.CONVERTED) {
            throw new BusinessException("该征集已转换为活动");
        }

        if (buddyRequest.getCurrentCount() < 2) {
            throw new BusinessException("至少需要2人才能创建活动");
        }

        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("【").append(buddyRequest.getType()).append("搭子活动】\n");
        if (buddyRequest.getDescription() != null) {
            descriptionBuilder.append(buddyRequest.getDescription()).append("\n\n");
        }
        descriptionBuilder.append("本活动由搭子征集帖转化而来，已成功配对").append(buddyRequest.getCurrentCount()).append("人。");

        ActivityCreateRequest activityRequest = ActivityCreateRequest.builder()
                .title(buddyRequest.getTitle())
                .type(buddyRequest.getType())
                .city(buddyRequest.getCity())
                .location(request.getLocation())
                .time(request.getTime())
                .maxParticipants(buddyRequest.getTargetCount())
                .description(descriptionBuilder.toString())
                .requirements(request.getRequirements())
                .image(request.getImage())
                .creatorId(request.getCreatorId())
                .build();

        ActivityResponse activity = activityService.createActivity(activityRequest);

        buddyRequest.setStatus(BuddyRequestStatus.CONVERTED);
        buddyRequest.setConvertedActivityId(activity.getId());
        buddyRequestRepository.save(buddyRequest);

        log.info("Converted buddy request: {} to activity: {}", request.getRequestId(), activity.getId());
        return activity;
    }

    private void sortBuddyRequests(List<BuddyRequest> requests, String sortBy) {
        switch (sortBy) {
            case "popular" -> requests.sort((a, b) -> b.getCurrentCount() - a.getCurrentCount());
            default -> requests.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }
    }

    private BuddyRequestResponse toRequestResponse(BuddyRequest request) {
        Integer applicationCount = (int) buddyApplicationRepository.countByBuddyRequestIdAndStatus(
                request.getId(), BuddyApplicationStatus.PENDING);

        return BuddyRequestResponse.builder()
                .id(request.getId())
                .title(request.getTitle())
                .type(request.getType())
                .city(request.getCity())
                .description(request.getDescription())
                .targetCount(request.getTargetCount())
                .currentCount(request.getCurrentCount())
                .status(request.getStatus())
                .convertedActivityId(request.getConvertedActivityId())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .creatorId(request.getCreator().getId())
                .creatorName(request.getCreator().getName())
                .creatorAvatar(request.getCreator().getAvatar())
                .applicationCount(applicationCount)
                .build();
    }

    private BuddyApplicationResponse toApplicationResponse(BuddyApplication application) {
        return BuddyApplicationResponse.builder()
                .id(application.getId())
                .requestId(application.getBuddyRequest().getId())
                .requestTitle(application.getBuddyRequest().getTitle())
                .requestType(application.getBuddyRequest().getType())
                .requestCity(application.getBuddyRequest().getCity())
                .applicantId(application.getApplicant().getId())
                .applicantName(application.getApplicant().getName())
                .applicantAvatar(application.getApplicant().getAvatar())
                .message(application.getMessage())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
