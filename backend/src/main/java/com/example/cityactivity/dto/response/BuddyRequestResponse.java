package com.example.cityactivity.dto.response;

import com.example.cityactivity.enums.BuddyRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuddyRequestResponse {

    private Long id;
    private String title;
    private String type;
    private String city;
    private String description;
    private Integer targetCount;
    private Integer currentCount;
    private BuddyRequestStatus status;
    private Long convertedActivityId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long creatorId;
    private String creatorName;
    private String creatorAvatar;
    private Integer applicationCount;
}
