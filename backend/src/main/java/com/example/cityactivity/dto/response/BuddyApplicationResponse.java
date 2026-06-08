package com.example.cityactivity.dto.response;

import com.example.cityactivity.enums.BuddyApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuddyApplicationResponse {

    private Long id;
    private Long requestId;
    private String requestTitle;
    private String requestType;
    private String requestCity;
    private Long applicantId;
    private String applicantName;
    private String applicantAvatar;
    private String message;
    private BuddyApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
