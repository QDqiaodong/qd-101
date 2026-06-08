package com.example.cityactivity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuddyConvertActivityRequest {

    @NotNull(message = "征集帖ID不能为空")
    private Long requestId;

    @NotNull(message = "发起人ID不能为空")
    private Long creatorId;

    @NotBlank(message = "地点不能为空")
    private String location;

    @NotNull(message = "活动时间不能为空")
    private LocalDateTime time;

    private String requirements;

    private String image;
}
