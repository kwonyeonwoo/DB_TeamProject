package com.academicshare.backend.schedule.dto;

import java.time.LocalDateTime;

public record ScheduleUpdateRequest(
        String title,
        boolean titleProvided,
        LocalDateTime startAt,
        boolean startAtProvided,
        LocalDateTime endAt,
        boolean endAtProvided,
        String description,
        boolean descriptionProvided,
        Integer type,
        boolean typeProvided
) {

    public boolean hasAnyField() {
        return titleProvided
                || startAtProvided
                || endAtProvided
                || descriptionProvided
                || typeProvided;
    }
}
