package com.example.cityactivity.service.impl;

import com.example.cityactivity.dto.request.CommentCreateRequest;
import com.example.cityactivity.dto.response.CommentCategoryStats;
import com.example.cityactivity.dto.response.CommentResponse;
import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.Comment;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.exception.BusinessException;
import com.example.cityactivity.exception.ResourceNotFoundException;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.repository.CommentRepository;
import com.example.cityactivity.repository.UserRepository;
import com.example.cityactivity.service.CommentService;
import com.example.cityactivity.service.ContentReviewService;
import com.example.cityactivity.enums.RiskLevel;
import com.example.cityactivity.dto.response.ContentReviewResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ContentReviewService contentReviewService;

    @Override
    @Transactional
    @CacheEvict(value = {"activity_comments", "comment_stats"}, allEntries = true)
    public CommentResponse createComment(CommentCreateRequest request) {
        ContentReviewResult reviewResult = contentReviewService.reviewText(request.getContent(), "comment");
        if (!reviewResult.isPassed()) {
            log.warn("Comment content review failed for user {}: {}",
                    request.getUserId(), reviewResult.getSuggestion());
            if (reviewResult.getOverallRiskLevel() == RiskLevel.HIGH) {
                throw new BusinessException("评论内容审核未通过：" + reviewResult.getSuggestion());
            }
        }

        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity", request.getActivityId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Comment parent = null;
        User replyToUser = null;

        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", request.getParentId()));
            if (!parent.getActivity().getId().equals(request.getActivityId())) {
                throw new BusinessException("父评论不属于该活动");
            }
        }

        if (request.getReplyToUserId() != null) {
            replyToUser = userRepository.findById(request.getReplyToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getReplyToUserId()));
        }

        Comment comment = Comment.builder()
                .activity(activity)
                .user(user)
                .content(request.getContent())
                .parent(parent)
                .replyToUser(replyToUser)
                .category(request.getCategory())
                .likes(0)
                .isPinned(false)
                .createdAt(LocalDateTime.now())
                .replies(new ArrayList<>())
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("Created comment: {} for activity: {}", saved.getId(), activity.getId());
        return toResponse(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activity_comments", key = "#activityId + ':' + #category")
    public List<CommentResponse> getCommentsByActivityId(Long activityId, String category) {
        List<Comment> rootComments;
        if (category != null && !category.isEmpty()) {
            rootComments = commentRepository.findRootCommentsByActivityIdAndCategory(activityId, category);
        } else {
            rootComments = commentRepository.findRootCommentsByActivityId(activityId);
        }

        return rootComments.stream()
                .map(comment -> toResponseWithReplies(comment))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comment_stats", key = "#activityId")
    public List<CommentCategoryStats> getCommentCategoryStats(Long activityId) {
        List<Object[]> results = commentRepository.countByActivityIdGroupByCategory(activityId);
        return results.stream()
                .map(row -> CommentCategoryStats.builder()
                        .category((String) row[0])
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommentCount(Long activityId) {
        return commentRepository.countByActivityId(activityId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "activity_comments", allEntries = true)
    public CommentResponse likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        comment.setLikes(comment.getLikes() + 1);
        Comment saved = commentRepository.save(comment);
        return toResponse(saved, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"activity_comments", "comment_stats"}, allEntries = true)
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException("无权限删除该评论");
        }

        commentRepository.delete(comment);
        log.info("Deleted comment: {}", commentId);
    }

    private CommentResponse toResponseWithReplies(Comment comment) {
        List<Comment> replies = commentRepository.findRepliesByParentId(comment.getId());
        List<CommentResponse> replyResponses = replies.stream()
                .map(reply -> toResponse(reply, false))
                .collect(Collectors.toList());

        CommentResponse response = toResponse(comment, false);
        response.setReplies(replyResponses);
        return response;
    }

    private CommentResponse toResponse(Comment comment, boolean includeReplies) {
        CommentResponse.CommentResponseBuilder builder = CommentResponse.builder()
                .id(comment.getId())
                .activityId(comment.getActivity().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getName())
                .userAvatar(comment.getUser().getAvatar())
                .content(comment.getContent())
                .category(comment.getCategory())
                .likes(comment.getLikes())
                .isPinned(comment.getIsPinned())
                .createdAt(comment.getCreatedAt());

        if (comment.getParent() != null) {
            builder.parentId(comment.getParent().getId());
        }

        if (comment.getReplyToUser() != null) {
            builder.replyToUserId(comment.getReplyToUser().getId());
            builder.replyToUserName(comment.getReplyToUser().getName());
        }

        if (includeReplies && comment.getReplies() != null) {
            builder.replies(comment.getReplies().stream()
                    .map(reply -> toResponse(reply, false))
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
