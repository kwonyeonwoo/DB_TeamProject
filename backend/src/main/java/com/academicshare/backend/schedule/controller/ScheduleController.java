package com.academicshare.backend.schedule.controller;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.common.response.ItemsResponse;
import com.academicshare.backend.schedule.dto.ScheduleCreateRequest;
import com.academicshare.backend.schedule.dto.ScheduleResponse;
import com.academicshare.backend.schedule.dto.ScheduleUpdateRequest;
import com.academicshare.backend.schedule.service.ScheduleService;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/me/schedules")
    public ItemsResponse<ScheduleResponse> getMySchedules(
            @RequestParam(name = "start_at", required = false) String startAt,
            @RequestParam(name = "end_at", required = false) String endAt
    ) {
        return new ItemsResponse<>(scheduleService.getMySchedules(
                parseDateTime(startAt),
                parseDateTime(endAt)
        ));
    }

    @PostMapping(value = "/me/schedules", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse createMySchedule(@RequestBody JsonNode request) {
        return scheduleService.createMySchedule(new ScheduleCreateRequest(
                textValue(request, "title"),
                dateTimeValue(request, "start_at"),
                dateTimeValue(request, "end_at"),
                textValue(request, "description"),
                integerValue(request, "type")
        ));
    }

    @PatchMapping(value = "/me/schedules/{scheduleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScheduleResponse updateMySchedule(
            @PathVariable Integer scheduleId,
            @RequestBody JsonNode request
    ) {
        return scheduleService.updateMySchedule(scheduleId, new ScheduleUpdateRequest(
                textValue(request, "title"),
                request.has("title"),
                dateTimeValue(request, "start_at"),
                request.has("start_at"),
                dateTimeValue(request, "end_at"),
                request.has("end_at"),
                textValue(request, "description"),
                request.has("description"),
                integerValue(request, "type"),
                request.has("type")
        ));
    }

    @DeleteMapping("/me/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteMySchedule(@PathVariable Integer scheduleId) {
        scheduleService.deleteMySchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{groupId}/schedules")
    public ItemsResponse<ScheduleResponse> getGroupSchedules(
            @PathVariable Integer groupId,
            @RequestParam(name = "start_at", required = false) String startAt,
            @RequestParam(name = "end_at", required = false) String endAt
    ) {
        return new ItemsResponse<>(scheduleService.getGroupSchedules(
                groupId,
                parseDateTime(startAt),
                parseDateTime(endAt)
        ));
    }

    @PostMapping(value = "/groups/{groupId}/schedules", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse createGroupSchedule(
            @PathVariable Integer groupId,
            @RequestBody JsonNode request
    ) {
        return scheduleService.createGroupSchedule(groupId, new ScheduleCreateRequest(
                textValue(request, "title"),
                dateTimeValue(request, "start_at"),
                dateTimeValue(request, "end_at"),
                textValue(request, "description"),
                integerValue(request, "type")
        ));
    }

    @PatchMapping(value = "/groups/{groupId}/schedules/{scheduleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScheduleResponse updateGroupSchedule(
            @PathVariable Integer groupId,
            @PathVariable Integer scheduleId,
            @RequestBody JsonNode request
    ) {
        return scheduleService.updateGroupSchedule(groupId, scheduleId, new ScheduleUpdateRequest(
                textValue(request, "title"),
                request.has("title"),
                dateTimeValue(request, "start_at"),
                request.has("start_at"),
                dateTimeValue(request, "end_at"),
                request.has("end_at"),
                textValue(request, "description"),
                request.has("description"),
                integerValue(request, "type"),
                request.has("type")
        ));
    }

    @DeleteMapping("/groups/{groupId}/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteGroupSchedule(
            @PathVariable Integer groupId,
            @PathVariable Integer scheduleId
    ) {
        scheduleService.deleteGroupSchedule(groupId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    private String textValue(JsonNode request, String fieldName) {
        JsonNode value = request.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        return value.asText();
    }

    private LocalDateTime dateTimeValue(JsonNode request, String fieldName) {
        JsonNode value = request.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        return parseDateTime(value.asText());
    }

    private Integer integerValue(JsonNode request, String fieldName) {
        JsonNode value = request.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isIntegralNumber() || !value.canConvertToInt()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        return value.asInt();
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
