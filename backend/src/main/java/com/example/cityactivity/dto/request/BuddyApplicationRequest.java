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
public class BuddyApplicationRequest {

    @NotNull(message = "征集帖ID不能为空")
    private Long requestId;

    @NotNull(message = "申请人ID不能为空")
    private Long applicantId;

    private String message;
}
