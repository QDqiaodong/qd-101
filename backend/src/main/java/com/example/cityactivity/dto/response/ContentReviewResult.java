package com.example.cityactivity.dto.response;

import com.example.cityactivity.enums.RiskLevel;
import com.example.cityactivity.enums.RiskType;
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
public class ContentReviewResult {

    private boolean passed;

    private RiskLevel overallRiskLevel;

    @Builder.Default
    private List<RiskHitDetail> hitDetails = new ArrayList<>();

    private String suggestion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskHitDetail {
        private RiskType riskType;
        private RiskLevel riskLevel;
        private String matchedWord;
        private String fieldName;
        private String context;
    }

    public void addHitDetail(RiskHitDetail detail) {
        if (this.hitDetails == null) {
            this.hitDetails = new ArrayList<>();
        }
        this.hitDetails.add(detail);
    }

    public boolean hasRisk() {
        return hitDetails != null && !hitDetails.isEmpty();
    }
}
