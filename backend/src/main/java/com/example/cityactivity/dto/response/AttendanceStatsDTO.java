package com.example.cityactivity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatsDTO {
    
    private Integer totalConfirmed;
    private Integer attendanceConfirmed;
    private Integer attendancePending;
    private Integer attendanceDeclined;
}
