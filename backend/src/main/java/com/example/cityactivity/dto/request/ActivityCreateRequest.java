package com.example.cityactivity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCreateRequest {
    
    @NotBlank(message = "活动标题不能为空")
    private String title;
    
    @NotBlank(message = "活动类型不能为空")
    private String type;
    
    @NotBlank(message = "城市不能为空")
    private String city;
    
    @NotBlank(message = "地点不能为空")
    private String location;
    
    @NotNull(message = "活动时间不能为空")
    private LocalDateTime time;
    
    @Positive(message = "人数上限必须为正数")
    private Integer maxParticipants;
    
    @NotBlank(message = "活动描述不能为空")
    private String description;
    
    private String requirements;
    
    private String image;
    
    @NotNull(message = "创建者ID不能为空")
    private Long creatorId;
}
