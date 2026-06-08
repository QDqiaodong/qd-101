package com.example.cityactivity.service;

import com.example.cityactivity.dto.request.BuddyApplicationRequest;
import com.example.cityactivity.dto.request.BuddyConvertActivityRequest;
import com.example.cityactivity.dto.request.BuddyRequestCreateRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.BuddyApplicationResponse;
import com.example.cityactivity.dto.response.BuddyRequestResponse;

import java.util.List;

public interface BuddyService {

    BuddyRequestResponse createRequest(BuddyRequestCreateRequest request);

    BuddyRequestResponse getRequestById(Long id);

    List<BuddyRequestResponse> getAllRequests(String city, String type, String status, String sortBy);

    List<BuddyRequestResponse> getRequestsByCreator(Long creatorId);

    List<BuddyRequestResponse> getMatchRecommendations(Long userId, String city);

    BuddyApplicationResponse applyForBuddy(BuddyApplicationRequest request);

    List<BuddyApplicationResponse> getApplicationsByRequest(Long requestId);

    List<BuddyApplicationResponse> getApplicationsByApplicant(Long applicantId);

    BuddyApplicationResponse acceptApplication(Long applicationId, Long creatorId);

    BuddyApplicationResponse rejectApplication(Long applicationId, Long creatorId, String reason);

    BuddyApplicationResponse cancelApplication(Long applicationId, Long applicantId);

    BuddyRequestResponse closeRequest(Long requestId, Long creatorId);

    ActivityResponse convertToActivity(BuddyConvertActivityRequest request);
}
