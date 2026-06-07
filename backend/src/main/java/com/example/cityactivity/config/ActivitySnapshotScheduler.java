package com.example.cityactivity.config;

import com.example.cityactivity.service.ActivitySnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivitySnapshotScheduler {

    private final ActivitySnapshotService activitySnapshotService;

    @Scheduled(cron = "${activity.snapshot.cron:0 0 * * * *}")
    public void createHourlySnapshot() {
        log.info("Starting scheduled full hot activity snapshot task");
        try {
            activitySnapshotService.createSnapshotForAllCities();
            log.info("Scheduled full hot activity snapshot task completed");
        } catch (Exception e) {
            log.error("Scheduled full hot activity snapshot task failed", e);
        }
    }

    @Scheduled(cron = "${activity.snapshot.priority-cron:0 */15 * * * *}")
    public void createPrioritySnapshot() {
        log.info("Starting priority-based hot activity snapshot task");
        try {
            activitySnapshotService.createPrioritySnapshots();
            log.info("Priority-based hot activity snapshot task completed");
        } catch (Exception e) {
            log.error("Priority-based hot activity snapshot task failed", e);
        }
    }
}
