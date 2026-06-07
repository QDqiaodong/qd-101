package com.example.cityactivity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityHotSnapshotDTO {

    private String city;
    private String timeSlice;
    private LocalDateTime snapshotTime;
    private List<HotSnapshotDTO> rankings;
    private Integer totalActivities;
}
