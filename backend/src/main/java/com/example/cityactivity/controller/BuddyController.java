package com.example.cityactivity.controller;

import com.example.cityactivity.dto.request.BuddyApplicationRequest;
import com.example.cityactivity.dto.request.BuddyConvertActivityRequest;
import com.example.cityactivity.dto.request.BuddyRequestCreateRequest;
import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.ApiResponse;
import com.example.cityactivity.dto.response.BuddyApplicationResponse;
import com.example.cityactivity.dto.response.BuddyRequestResponse;
import com.example.cityactivity.service.BuddyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buddies")
@RequiredArgsConstructor
public class BuddyController {

    private final BuddyService buddyService;

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<BuddyRequestResponse>> createRequest(
            @Valid @RequestBody BuddyRequestCreateRequest request) {
        BuddyRequestResponse response = buddyService.createRequest(request);
        return ResponseEntity.ok(ApiResponse.success("搭子征集发布成功", response));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<BuddyRequestResponse>> getRequestById(@PathVariable Long id) {
        BuddyRequestResponse response = buddyService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<BuddyRequestResponse>>> getRequests(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "newest") String sortBy) {
        List<BuddyRequestResponse> responses = buddyService.getAllRequests(city, type, status, sortBy);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/requests/creator/{creatorId}")
    public ResponseEntity<ApiResponse<List<BuddyRequestResponse>>> getRequestsByCreator(
            @PathVariable Long creatorId) {
        List<BuddyRequestResponse> responses = buddyService.getRequestsByCreator(creatorId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<BuddyRequestResponse>>> getMatchRecommendations(
            @RequestParam Long userId,
            @RequestParam(required = false) String city) {
        List<BuddyRequestResponse> responses = buddyService.getMatchRecommendations(userId, city);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<BuddyApplicationResponse>> applyForBuddy(
            @Valid @RequestBody BuddyApplicationRequest request) {
        BuddyApplicationResponse response = buddyService.applyForBuddy(request);
        return ResponseEntity.ok(ApiResponse.success("申请成功", response));
    }

    @GetMapping("/applications/request/{requestId}")
    public ResponseEntity<ApiResponse<List<BuddyApplicationResponse>>> getApplicationsByRequest(
            @PathVariable Long requestId) {
        List<BuddyApplicationResponse> responses = buddyService.getApplicationsByRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/applications/applicant/{applicantId}")
    public ResponseEntity<ApiResponse<List<BuddyApplicationResponse>>> getApplicationsByApplicant(
            @PathVariable Long applicantId) {
        List<BuddyApplicationResponse> responses = buddyService.getApplicationsByApplicant(applicantId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/applications/{id}/accept")
    public ResponseEntity<ApiResponse<BuddyApplicationResponse>> acceptApplication(
            @PathVariable Long id,
            @RequestParam Long creatorId) {
        BuddyApplicationResponse response = buddyService.acceptApplication(id, creatorId);
        return ResponseEntity.ok(ApiResponse.success("已接受申请", response));
    }

    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<ApiResponse<BuddyApplicationResponse>> rejectApplication(
            @PathVariable Long id,
            @RequestParam Long creatorId,
            @RequestParam(required = false) String reason) {
        BuddyApplicationResponse response = buddyService.rejectApplication(id, creatorId, reason);
        return ResponseEntity.ok(ApiResponse.success("已拒绝申请", response));
    }

    @PostMapping("/applications/{id}/cancel")
    public ResponseEntity<ApiResponse<BuddyApplicationResponse>> cancelApplication(
            @PathVariable Long id,
            @RequestParam Long applicantId) {
        BuddyApplicationResponse response = buddyService.cancelApplication(id, applicantId);
        return ResponseEntity.ok(ApiResponse.success("已取消申请", response));
    }

    @PostMapping("/requests/{id}/close")
    public ResponseEntity<ApiResponse<BuddyRequestResponse>> closeRequest(
            @PathVariable Long id,
            @RequestParam Long creatorId) {
        BuddyRequestResponse response = buddyService.closeRequest(id, creatorId);
        return ResponseEntity.ok(ApiResponse.success("已关闭征集", response));
    }

    @PostMapping("/convert")
    public ResponseEntity<ApiResponse<ActivityResponse>> convertToActivity(
            @Valid @RequestBody BuddyConvertActivityRequest request) {
        ActivityResponse response = buddyService.convertToActivity(request);
        return ResponseEntity.ok(ApiResponse.success("已转化为正式活动", response));
    }
}
