package com.example.cityactivity.service;

import com.example.cityactivity.dto.request.CommentCreateRequest;
import com.example.cityactivity.dto.response.CommentCategoryStats;
import com.example.cityactivity.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(CommentCreateRequest request);

    List<CommentResponse> getCommentsByActivityId(Long activityId, String category);

    List<CommentCategoryStats> getCommentCategoryStats(Long activityId);

    long getCommentCount(Long activityId);

    CommentResponse likeComment(Long commentId, Long userId);

    void deleteComment(Long commentId, Long userId);
}
