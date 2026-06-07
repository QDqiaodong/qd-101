package com.example.cityactivity.enums;

public enum RiskType {
    VULGAR_INVITATION("低俗邀约", "包含低俗、色情暗示或不当交友邀约内容"),
    GRAY_MARKETING("灰产导流", "包含赌博、传销、兼职刷单等灰产导流内容"),
    FAKE_FEE("虚假收费", "包含虚假收费、诈骗或不合理敛财内容"),
    LOCATION_INDUCTION("地点诱导", "包含可疑地点引导或线下危险会面诱导");

    private final String displayName;
    private final String description;

    RiskType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
