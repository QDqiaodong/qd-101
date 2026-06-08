package com.example.cityactivity.dto.response;

import com.example.cityactivity.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationUserResponse {
    
    private Long userId;
    private String userName;
    private String userAvatar;
    private LocalDateTime registeredAt;
    private AttendanceStatus attendanceStatus;
    private LocalDateTime attendanceConfirmedAt;
    private Integer waitlistPosition;
}
