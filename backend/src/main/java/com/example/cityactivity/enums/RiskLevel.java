package com.example.cityactivity.enums;

public enum RiskLevel {
    SAFE(0, "安全", "内容合规，正常发布"),
    LOW(1, "低风险", "内容存在轻微敏感词，建议人工复核后发布"),
    MEDIUM(2, "中风险", "内容存在较明显风险，需人工审核通过后方可发布"),
    HIGH(3, "高风险", "内容存在严重违规，直接拦截禁止发布");

    private final int level;
    private final String displayName;
    private final String description;

    RiskLevel(int level, String displayName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
