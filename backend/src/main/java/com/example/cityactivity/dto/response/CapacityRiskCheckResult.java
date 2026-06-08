package com.example.cityactivity.dto.response;

import com.example.cityactivity.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacityRiskCheckResult {

    private boolean passed;

    private RiskLevel overallRiskLevel;

    private String activityType;

    private int maxParticipants;

    private List<CapacityRiskIssue> issues;

    private String suggestion;

    public static CapacityRiskCheckResult pass(String activityType, int maxParticipants) {
        return CapacityRiskCheckResult.builder()
                .passed(true)
                .overallRiskLevel(RiskLevel.SAFE)
                .activityType(activityType)
                .maxParticipants(maxParticipants)
                .issues(new ArrayList<>())
                .suggestion("容量符合风控规则")
                .build();
    }

    public static CapacityRiskCheckResult warn(String activityType, int maxParticipants, List<CapacityRiskIssue> issues, String suggestion) {
        return CapacityRiskCheckResult.builder()
                .passed(true)
                .overallRiskLevel(RiskLevel.LOW)
                .activityType(activityType)
                .maxParticipants(maxParticipants)
                .issues(issues)
                .suggestion(suggestion)
                .build();
    }

    public static CapacityRiskCheckResult medium(String activityType, int maxParticipants, List<CapacityRiskIssue> issues, String suggestion) {
        return CapacityRiskCheckResult.builder()
                .passed(false)
                .overallRiskLevel(RiskLevel.MEDIUM)
                .activityType(activityType)
                .maxParticipants(maxParticipants)
                .issues(issues)
                .suggestion(suggestion)
                .build();
    }

    public static CapacityRiskCheckResult high(String activityType, int maxParticipants, List<CapacityRiskIssue> issues, String suggestion) {
        return CapacityRiskCheckResult.builder()
                .passed(false)
                .overallRiskLevel(RiskLevel.HIGH)
                .activityType(activityType)
                .maxParticipants(maxParticipants)
                .issues(issues)
                .suggestion(suggestion)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacityRiskIssue {
        private String code;
        private String description;
        private RiskLevel riskLevel;
        private String ruleDescription;
    }
}
