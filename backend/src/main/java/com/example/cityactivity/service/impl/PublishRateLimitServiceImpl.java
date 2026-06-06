package com.example.cityactivity.service.impl;

import com.example.cityactivity.config.PublishRateLimitProperties;
import com.example.cityactivity.dto.request.ActivityCreateRequest;
import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.exception.BusinessException;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.service.PublishRateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublishRateLimitServiceImpl implements PublishRateLimitService {
    
    private final ActivityRepository activityRepository;
    private final PublishRateLimitProperties properties;
    
    @Override
    public void checkPublishRate(Long creatorId, ActivityCreateRequest request) {
        checkCooldown(creatorId);
        checkDailyLimit(creatorId);
        checkSimilarContent(creatorId, request);
    }
    
    private void checkCooldown(Long creatorId) {
        Optional<Activity> lastActivity = activityRepository.findTopByCreatorIdOrderByCreatedAtDesc(creatorId);
        
        if (lastActivity.isPresent()) {
            LocalDateTime lastPublishTime = lastActivity.get().getCreatedAt();
            LocalDateTime earliestNextPublish = lastPublishTime.plusSeconds(properties.getCooldownSeconds());
            
            if (LocalDateTime.now().isBefore(earliestNextPublish)) {
                long waitSeconds = java.time.Duration.between(LocalDateTime.now(), earliestNextPublish).getSeconds();
                long waitMinutes = waitSeconds / 60;
                long remainingSeconds = waitSeconds % 60;
                
                String waitMessage;
                if (waitMinutes > 0) {
                    waitMessage = String.format("%d分%d秒", waitMinutes, remainingSeconds);
                } else {
                    waitMessage = String.format("%d秒", remainingSeconds);
                }
                
                log.warn("Cooldown check failed for creator {}: need to wait {}", creatorId, waitMessage);
                throw new BusinessException("发布过于频繁，请稍后再试，距离下次发布还需 " + waitMessage);
            }
        }
    }
    
    private void checkDailyLimit(Long creatorId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayCount = activityRepository.countByCreatorIdAndCreatedAtAfter(creatorId, startOfDay);
        
        if (todayCount >= properties.getDailyLimit()) {
            log.warn("Daily limit check failed for creator {}: {} activities today", creatorId, todayCount);
            throw new BusinessException("今日发布数量已达上限（" + properties.getDailyLimit() + "个），请明天再试");
        }
    }
    
    private void checkSimilarContent(Long creatorId, ActivityCreateRequest request) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(properties.getSimilarContentWindowHours());
        
        List<Activity> recentActivities = activityRepository.findSimilarActivities(
                creatorId,
                request.getType(),
                request.getCity(),
                request.getLocation(),
                startTime
        );
        
        for (Activity activity : recentActivities) {
            double similarity = calculateTitleSimilarity(request.getTitle(), activity.getTitle());
            if (similarity >= properties.getSimilarTitleThreshold()) {
                log.warn("Similar content check failed for creator {}: similarity {} with activity {}",
                        creatorId, similarity, activity.getId());
                throw new BusinessException("检测到您近期已发布过相似活动（标题相似度" + 
                        String.format("%.0f%%", similarity * 100) + 
                        "），请修改内容后再发布");
            }
        }
    }
    
    private double calculateTitleSimilarity(String title1, String title2) {
        if (title1 == null || title2 == null) {
            return 0.0;
        }
        if (title1.equals(title2)) {
            return 1.0;
        }
        
        Set<Character> chars1 = new HashSet<>();
        Set<Character> chars2 = new HashSet<>();
        
        for (char c : title1.toLowerCase().toCharArray()) {
            if (!Character.isWhitespace(c)) {
                chars1.add(c);
            }
        }
        
        for (char c : title2.toLowerCase().toCharArray()) {
            if (!Character.isWhitespace(c)) {
                chars2.add(c);
            }
        }
        
        if (chars1.isEmpty() && chars2.isEmpty()) {
            return 1.0;
        }
        if (chars1.isEmpty() || chars2.isEmpty()) {
            return 0.0;
        }
        
        Set<Character> intersection = new HashSet<>(chars1);
        intersection.retainAll(chars2);
        
        Set<Character> union = new HashSet<>(chars1);
        union.addAll(chars2);
        
        return (double) intersection.size() / union.size();
    }
}
