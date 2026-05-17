package com.academicshare.backend.schedule.dto;

import java.time.LocalDateTime;

public record ScheduleCreateRequest(
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String description,
        Integer type
) {
}
