package com.example.cityactivity.controller;

import com.example.cityactivity.dto.request.CommentCreateRequest;
import com.example.cityactivity.dto.response.ApiResponse;
import com.example.cityactivity.dto.response.CommentCategoryStats;
import com.example.cityactivity.dto.response.CommentResponse;
import com.example.cityactivity.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@Valid @RequestBody CommentCreateRequest request) {
        CommentResponse comment = commentService.createComment(request);
        return ResponseEntity.ok(ApiResponse.success("评论发布成功", comment));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByActivityId(
            @PathVariable Long activityId,
            @RequestParam(required = false) String category) {
        List<CommentResponse> comments = commentService.getCommentsByActivityId(activityId, category);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @GetMapping("/activity/{activityId}/stats")
    public ResponseEntity<ApiResponse<List<CommentCategoryStats>>> getCommentCategoryStats(@PathVariable Long activityId) {
        List<CommentCategoryStats> stats = commentService.getCommentCategoryStats(activityId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/activity/{activityId}/count")
    public ResponseEntity<ApiResponse<Long>> getCommentCount(@PathVariable Long activityId) {
        long count = commentService.getCommentCount(activityId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<CommentResponse>> likeComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        CommentResponse comment = commentService.likeComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success(comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
