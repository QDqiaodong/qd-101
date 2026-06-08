package com.example.cityactivity.service.impl;

import com.example.cityactivity.config.CapacityRiskRuleConfig;
import com.example.cityactivity.dto.response.CapacityRiskCheckResult;
import com.example.cityactivity.enums.RiskLevel;
import com.example.cityactivity.service.CapacityRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapacityRiskServiceImpl implements CapacityRiskService {

    private final CapacityRiskRuleConfig ruleConfig;

    @Override
    public CapacityRiskCheckResult checkActivityCapacity(String activityType, int maxParticipants) {
        CapacityRiskRuleConfig.CapacityRiskRule rule = ruleConfig.getRule(activityType);
        List<CapacityRiskCheckResult.CapacityRiskIssue> issues = new ArrayList<>();

        if (maxParticipants <= 0) {
            issues.add(CapacityRiskCheckResult.CapacityRiskIssue.builder()
                    .code("INVALID_CAPACITY")
                    .description("活动人数必须为正数")
                    .riskLevel(RiskLevel.HIGH)
                    .ruleDescription("活动人数上限必须大于0")
                    .build());
            return CapacityRiskCheckResult.high(activityType, maxParticipants, issues, "活动人数无效");
        }

        if (maxParticipants == 1) {
            issues.add(CapacityRiskCheckResult.CapacityRiskIssue.builder()
                    .code("CAPACITY_SINGLE_PERSON")
                    .description("单人活动不符合社交活动属性")
                    .riskLevel(RiskLevel.HIGH)
                    .ruleDescription("活动至少需要2人参与才有社交意义")
                    .build());
            return CapacityRiskCheckResult.high(activityType, maxParticipants, issues,
                    "单人活动不符合平台社交属性，请增加参与人数");
        }

        if (maxParticipants < rule.getMinNormalParticipants()) {
            issues.add(CapacityRiskCheckResult.CapacityRiskIssue.builder()
                    .code("CAPACITY_TOO_LOW")
                    .description(String.format("人数低于%s活动的合理范围(%d人起)", activityType, rule.getMinNormalParticipants()))
                    .riskLevel(RiskLevel.MEDIUM)
                    .ruleDescription(rule.getDescription())
                    .build());
            return CapacityRiskCheckResult.medium(activityType, maxParticipants, issues,
                    String.format("活动人数(%d人)低于%s类活动的合理范围(%d人起)，存在数据失真风险。如确为小型活动，请联系平台审核",
                            maxParticipants, activityType, rule.getMinNormalParticipants()));
        }

        if (maxParticipants > rule.getMaxHighRiskParticipants()) {
            issues.add(CapacityRiskCheckResult.CapacityRiskIssue.builder()
                    .code("CAPACITY_EXTREMELY_HIGH")
                    .description(String.format("人数严重超出%s活动的合理范围(上限%d人)", activityType, rule.getMaxHighRiskParticipants()))
                    .riskLevel(RiskLevel.HIGH)
                    .ruleDescription(rule.getDescription())
                    .build());
            return CapacityRiskCheckResult.high(activityType, maxParticipants, issues,
                    String.format("活动人数(%d人)严重超出%s类活动的合理范围(%d人上限)，存在高风险，请调整人数后重新发布",
                            maxParticipants, activityType, rule.getMaxHighRiskParticipants()));
        }

        if (maxParticipants > rule.getMaxWarningParticipants()) {
            issues.add(CapacityRiskCheckResult.CapacityRiskIssue.builder()
                    .code("CAPACITY_HIGH_WARNING")
                    .description(String.format("人数超出%s活动的正常范围(%d人内为宜)", activityType, rule.getMaxNormalParticipants()))
                    .riskLevel(RiskLevel.MEDIUM)
                    .ruleDescription(rule.getDescription())
                    .build());
            return CapacityRiskCheckResult.medium(activityType, maxParticipants, issues,
                    String.format("活动人数(%d人)超出%s类活动的正常范围，建议控制在%d人以内。如确需举办大型活动，请联系平台审核",
                            maxParticipants, activityType, rule.getMaxNormalParticipants()));
        }

        if (maxParticipants > rule.getMaxNormalParticipants()) {
            issues.add(CapacityRiskCheckResult.CapacityRiskIssue.builder()
                    .code("CAPACITY_SLIGHTLY_HIGH")
                    .description(String.format("人数略高于%s活动的常见规模(%d人内)", activityType, rule.getMaxNormalParticipants()))
                    .riskLevel(RiskLevel.LOW)
                    .ruleDescription(rule.getDescription())
                    .build());
            return CapacityRiskCheckResult.warn(activityType, maxParticipants, issues,
                    String.format("活动人数(%d人)略高于%s类活动的常见规模，请注意活动组织和安全保障",
                            maxParticipants, activityType));
        }

        return CapacityRiskCheckResult.pass(activityType, maxParticipants);
    }

    @Override
    public CapacityRiskCheckResult checkCapacityExpansion(String activityType, int oldMaxParticipants, int newMaxParticipants) {
        CapacityRiskRuleConfig.CapacityRiskRule rule = ruleConfig.getRule(activityType);
        List<CapacityRiskCheckResult.CapacityRiskIssue> issues = new ArrayList<>();

        if (newMaxParticipants <= oldMaxParticipants) {
            return checkActivityCapacity(activityType, newMaxParticipants);
        }

        double expansionRate = (double) newMaxParticipants / oldMaxParticipants;

        if (expansionRate > rule.getMaxExpansionRate()) {
            double maxExpanded = oldMaxParticipants * rule.getMaxExpansionRate();
            issues.add(CapacityRiskCheckResult.CapacityRiskIssue.builder()
                    .code("ABNORMAL_EXPANSION")
                    .description(String.format("扩容幅度过大：从%d人增至%d人，增幅%.1f%%，超过%s活动的正常扩容节奏(%.0f%%以内)",
                            oldMaxParticipants, newMaxParticipants, (expansionRate - 1) * 100,
                            activityType, (rule.getMaxExpansionRate() - 1) * 100))
                    .riskLevel(RiskLevel.MEDIUM)
                    .ruleDescription(String.format("%s活动单次扩容不超过%.0f%%，且%d小时内累计扩容不得超过该比例",
                            activityType, (rule.getMaxExpansionRate() - 1) * 100, rule.getExpansionCheckHours()))
                    .build());

            CapacityRiskCheckResult capacityResult = checkActivityCapacity(activityType, newMaxParticipants);
            issues.addAll(capacityResult.getIssues());

            RiskLevel overallLevel = capacityResult.getOverallRiskLevel().getLevel() > RiskLevel.MEDIUM.getLevel()
                    ? capacityResult.getOverallRiskLevel() : RiskLevel.MEDIUM;

            if (overallLevel == RiskLevel.HIGH) {
                return CapacityRiskCheckResult.high(activityType, newMaxParticipants, issues,
                        String.format("扩容异常：人数从%d增至%d，增幅过大且超出活动类型合理范围。建议单次扩容不超过%.0f%%，最终人数不超过%d人",
                                oldMaxParticipants, newMaxParticipants,
                                (rule.getMaxExpansionRate() - 1) * 100, rule.getMaxWarningParticipants()));
            }

            return CapacityRiskCheckResult.medium(activityType, newMaxParticipants, issues,
                    String.format("扩容幅度过大：从%d人增至%d人(增幅%.1f%%)。建议%s类活动单次扩容控制在%.0f%%以内，即最多%d人",
                            oldMaxParticipants, newMaxParticipants, (expansionRate - 1) * 100,
                            activityType, (rule.getMaxExpansionRate() - 1) * 100, (int) Math.floor(maxExpanded)));
        }

        return checkActivityCapacity(activityType, newMaxParticipants);
    }
}
