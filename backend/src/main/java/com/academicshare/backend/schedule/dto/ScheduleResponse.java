package com.academicshare.backend.schedule.dto;

import com.academicshare.backend.schedule.domain.Schedule;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ScheduleResponse(
        Integer id,
        Integer userId,
        Integer groupId,
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String description,
        Integer type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getUserId(),
                schedule.getGroupId(),
                schedule.getTitle(),
                schedule.getStartAt(),
                schedule.getEndAt(),
                schedule.getDescription(),
                schedule.getType(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}
