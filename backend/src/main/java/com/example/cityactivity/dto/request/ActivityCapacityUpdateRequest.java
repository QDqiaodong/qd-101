package com.example.cityactivity.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCapacityUpdateRequest {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "创建者ID不能为空")
    private Long creatorId;

    @Positive(message = "人数上限必须为正数")
    private Integer newMaxParticipants;
}
