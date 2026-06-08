package com.example.cityactivity.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class BuddyRequestCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "搭子类型不能为空")
    private String type;

    @NotBlank(message = "城市不能为空")
    private String city;

    private String description;

    @Positive(message = "目标人数必须为正数")
    private Integer targetCount;

    @NotNull(message = "创建者ID不能为空")
    private Long creatorId;
}
