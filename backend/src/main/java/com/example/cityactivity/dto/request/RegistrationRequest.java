package com.example.cityactivity.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
