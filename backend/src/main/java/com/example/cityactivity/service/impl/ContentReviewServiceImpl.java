package com.example.cityactivity.service.impl;

import com.example.cityactivity.config.RiskWordConfig;
import com.example.cityactivity.dto.request.ActivityCreateRequest;
import com.example.cityactivity.dto.response.ContentReviewResult;
import com.example.cityactivity.enums.RiskLevel;
import com.example.cityactivity.enums.RiskType;
import com.example.cityactivity.service.ContentReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentReviewServiceImpl implements ContentReviewService {

    private final RiskWordConfig riskWordConfig;

    private static final int CONTEXT_RADIUS = 10;

    @Override
    public ContentReviewResult reviewActivityContent(ActivityCreateRequest request) {
        List<ContentReviewResult.RiskHitDetail> allHits = new ArrayList<>();

        allHits.addAll(reviewTextField(request.getTitle(), "title"));
        allHits.addAll(reviewTextField(request.getDescription(), "description"));
        allHits.addAll(reviewTextField(request.getLocation(), "location"));
        allHits.addAll(reviewTextField(request.getRequirements(), "requirements"));
        allHits.addAll(reviewTextField(request.getType(), "type"));

        RiskLevel overallLevel = calculateOverallRiskLevel(allHits);
        boolean passed = overallLevel == RiskLevel.SAFE || overallLevel == RiskLevel.LOW;
        String suggestion = generateSuggestion(overallLevel, allHits);

        log.info("Content review completed. Overall risk: {}, hits: {}, passed: {}",
                overallLevel, allHits.size(), passed);

        return ContentReviewResult.builder()
                .passed(passed)
                .overallRiskLevel(overallLevel)
                .hitDetails(allHits)
                .suggestion(suggestion)
                .build();
    }

    @Override
    public ContentReviewResult reviewText(String text, String fieldName) {
        List<ContentReviewResult.RiskHitDetail> hits = reviewTextField(text, fieldName);
        RiskLevel overallLevel = calculateOverallRiskLevel(hits);
        boolean passed = overallLevel == RiskLevel.SAFE || overallLevel == RiskLevel.LOW;
        String suggestion = generateSuggestion(overallLevel, hits);

        return ContentReviewResult.builder()
                .passed(passed)
                .overallRiskLevel(overallLevel)
                .hitDetails(hits)
                .suggestion(suggestion)
                .build();
    }

    private List<ContentReviewResult.RiskHitDetail> reviewTextField(String text, String fieldName) {
        List<ContentReviewResult.RiskHitDetail> hits = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return hits;
        }

        String lowerText = text.toLowerCase();

        for (RiskWordConfig.RiskWordEntry entry : riskWordConfig.getRiskWords()) {
            String word = entry.getWord().toLowerCase();
            int index = lowerText.indexOf(word);

            while (index >= 0) {
                String context = extractContext(text, index, word.length());

                ContentReviewResult.RiskHitDetail detail = ContentReviewResult.RiskHitDetail.builder()
                        .riskType(entry.getRiskType())
                        .riskLevel(entry.getRiskLevel())
                        .matchedWord(entry.getWord())
                        .fieldName(fieldName)
                        .context(context)
                        .build();

                hits.add(detail);
                index = lowerText.indexOf(word, index + word.length());
            }
        }

        return hits;
    }

    private String extractContext(String text, int matchStart, int matchLength) {
        int start = Math.max(0, matchStart - CONTEXT_RADIUS);
        int end = Math.min(text.length(), matchStart + matchLength + CONTEXT_RADIUS);

        StringBuilder sb = new StringBuilder();
        if (start > 0) {
            sb.append("...");
        }
        sb.append(text, start, end);
        if (end < text.length()) {
            sb.append("...");
        }

        return sb.toString();
    }

    private RiskLevel calculateOverallRiskLevel(List<ContentReviewResult.RiskHitDetail> hits) {
        if (hits == null || hits.isEmpty()) {
            return RiskLevel.SAFE;
        }

        RiskLevel maxLevel = RiskLevel.SAFE;
        for (ContentReviewResult.RiskHitDetail hit : hits) {
            if (hit.getRiskLevel().getLevel() > maxLevel.getLevel()) {
                maxLevel = hit.getRiskLevel();
            }
        }

        int highCount = 0;
        int mediumCount = 0;
        for (ContentReviewResult.RiskHitDetail hit : hits) {
            if (hit.getRiskLevel() == RiskLevel.HIGH) {
                highCount++;
            } else if (hit.getRiskLevel() == RiskLevel.MEDIUM) {
                mediumCount++;
            }
        }

        if (highCount >= 3) {
            return RiskLevel.HIGH;
        }
        if (mediumCount >= 5) {
            return RiskLevel.HIGH;
        }
        if (mediumCount >= 3 && maxLevel == RiskLevel.MEDIUM) {
            return RiskLevel.HIGH;
        }

        return maxLevel;
    }

    private String generateSuggestion(RiskLevel overallLevel, List<ContentReviewResult.RiskHitDetail> hits) {
        return switch (overallLevel) {
            case SAFE -> "内容审核通过，可正常发布。";
            case LOW -> {
                StringBuilder sb = new StringBuilder();
                sb.append("检测到").append(hits.size()).append("处低风险内容，建议人工复核后发布。涉及风险类型：");
                sb.append(getRiskTypesSummary(hits));
                yield sb.toString();
            }
            case MEDIUM -> {
                StringBuilder sb = new StringBuilder();
                sb.append("检测到").append(hits.size()).append("处中风险内容，需人工审核通过后方可发布。涉及风险类型：");
                sb.append(getRiskTypesSummary(hits));
                yield sb.toString();
            }
            case HIGH -> {
                StringBuilder sb = new StringBuilder();
                sb.append("检测到").append(hits.size()).append("处高风险内容，已拦截禁止发布。涉及风险类型：");
                sb.append(getRiskTypesSummary(hits));
                yield sb.toString();
            }
        };
    }

    private String getRiskTypesSummary(List<ContentReviewResult.RiskHitDetail> hits) {
        List<String> types = new ArrayList<>();
        for (ContentReviewResult.RiskHitDetail hit : hits) {
            String typeName = hit.getRiskType().getDisplayName();
            if (!types.contains(typeName)) {
                types.add(typeName);
            }
        }
        return String.join("、", types);
    }
}
