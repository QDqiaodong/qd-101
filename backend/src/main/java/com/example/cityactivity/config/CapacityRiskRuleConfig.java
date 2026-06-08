package com.example.cityactivity.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CapacityRiskRuleConfig {

    @Getter
    private final Map<String, CapacityRiskRule> riskRules = new HashMap<>();

    public CapacityRiskRuleConfig() {
        initDiningRule();
        initBoardGameRule();
        initHikingRule();
        initBallGameRule();
        initExplorationRule();
        initOtherRule();
    }

    private void initDiningRule() {
        riskRules.put("聚餐", CapacityRiskRule.builder()
                .activityType("聚餐")
                .minNormalParticipants(4)
                .maxNormalParticipants(15)
                .maxWarningParticipants(25)
                .maxHighRiskParticipants(40)
                .maxExpansionRate(1.5)
                .expansionCheckHours(24)
                .description("聚餐活动通常适合小团体，人数过多会影响用餐体验和组织难度")
                .build());
    }

    private void initBoardGameRule() {
        riskRules.put("桌游", CapacityRiskRule.builder()
                .activityType("桌游")
                .minNormalParticipants(4)
                .maxNormalParticipants(12)
                .maxWarningParticipants(20)
                .maxHighRiskParticipants(30)
                .maxExpansionRate(1.4)
                .expansionCheckHours(24)
                .description("桌游活动受场地和游戏类型限制，人数过多会降低参与感")
                .build());
    }

    private void initHikingRule() {
        riskRules.put("徒步", CapacityRiskRule.builder()
                .activityType("徒步")
                .minNormalParticipants(5)
                .maxNormalParticipants(20)
                .maxWarningParticipants(35)
                .maxHighRiskParticipants(50)
                .maxExpansionRate(1.6)
                .expansionCheckHours(48)
                .description("徒步活动需考虑安全管理和领队配比，人数过多存在安全隐患")
                .build());
    }

    private void initBallGameRule() {
        riskRules.put("打球", CapacityRiskRule.builder()
                .activityType("打球")
                .minNormalParticipants(2)
                .maxNormalParticipants(12)
                .maxWarningParticipants(20)
                .maxHighRiskParticipants(30)
                .maxExpansionRate(1.3)
                .expansionCheckHours(24)
                .description("球类活动受场地限制，人数过多会降低运动体验")
                .build());
    }

    private void initExplorationRule() {
        riskRules.put("探店", CapacityRiskRule.builder()
                .activityType("探店")
                .minNormalParticipants(3)
                .maxNormalParticipants(10)
                .maxWarningParticipants(15)
                .maxHighRiskParticipants(25)
                .maxExpansionRate(1.4)
                .expansionCheckHours(24)
                .description("探店活动受商家接待能力限制，人数过多会影响体验")
                .build());
    }

    private void initOtherRule() {
        riskRules.put("其他", CapacityRiskRule.builder()
                .activityType("其他")
                .minNormalParticipants(2)
                .maxNormalParticipants(20)
                .maxWarningParticipants(35)
                .maxHighRiskParticipants(50)
                .maxExpansionRate(1.5)
                .expansionCheckHours(24)
                .description("其他类型活动的默认容量规则")
                .build());
    }

    public CapacityRiskRule getRule(String activityType) {
        CapacityRiskRule rule = riskRules.get(activityType);
        return rule != null ? rule : riskRules.get("其他");
    }

    @Getter
    public static class CapacityRiskRule {
        private final String activityType;
        private final int minNormalParticipants;
        private final int maxNormalParticipants;
        private final int maxWarningParticipants;
        private final int maxHighRiskParticipants;
        private final double maxExpansionRate;
        private final int expansionCheckHours;
        private final String description;

        private CapacityRiskRule(Builder builder) {
            this.activityType = builder.activityType;
            this.minNormalParticipants = builder.minNormalParticipants;
            this.maxNormalParticipants = builder.maxNormalParticipants;
            this.maxWarningParticipants = builder.maxWarningParticipants;
            this.maxHighRiskParticipants = builder.maxHighRiskParticipants;
            this.maxExpansionRate = builder.maxExpansionRate;
            this.expansionCheckHours = builder.expansionCheckHours;
            this.description = builder.description;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String activityType;
            private int minNormalParticipants;
            private int maxNormalParticipants;
            private int maxWarningParticipants;
            private int maxHighRiskParticipants;
            private double maxExpansionRate;
            private int expansionCheckHours;
            private String description;

            public Builder activityType(String activityType) {
                this.activityType = activityType;
                return this;
            }

            public Builder minNormalParticipants(int minNormalParticipants) {
                this.minNormalParticipants = minNormalParticipants;
                return this;
            }

            public Builder maxNormalParticipants(int maxNormalParticipants) {
                this.maxNormalParticipants = maxNormalParticipants;
                return this;
            }

            public Builder maxWarningParticipants(int maxWarningParticipants) {
                this.maxWarningParticipants = maxWarningParticipants;
                return this;
            }

            public Builder maxHighRiskParticipants(int maxHighRiskParticipants) {
                this.maxHighRiskParticipants = maxHighRiskParticipants;
                return this;
            }

            public Builder maxExpansionRate(double maxExpansionRate) {
                this.maxExpansionRate = maxExpansionRate;
                return this;
            }

            public Builder expansionCheckHours(int expansionCheckHours) {
                this.expansionCheckHours = expansionCheckHours;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public CapacityRiskRule build() {
                return new CapacityRiskRule(this);
            }
        }
    }
}
