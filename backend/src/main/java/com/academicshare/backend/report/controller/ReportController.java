package com.academicshare.backend.report.controller;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.common.response.ItemsResponse;
import com.academicshare.backend.report.domain.ReportStatus;
import com.academicshare.backend.report.domain.ReportTargetType;
import com.academicshare.backend.report.dto.ReportCreateRequest;
import com.academicshare.backend.report.dto.ReportProcessRequest;
import com.academicshare.backend.report.dto.ReportResponse;
import com.academicshare.backend.report.service.ReportService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping(value = "/reports", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse createReport(@RequestBody JsonNode request) {
        return reportService.createReport(new ReportCreateRequest(
                targetTypeValue(request, "target_type"),
                integerValue(request, "target_id"),
                integerValue(request, "reason_type")
        ));
    }

    @GetMapping("/admin/reports")
    public ItemsResponse<ReportResponse> getReports() {
        return new ItemsResponse<>(reportService.getReports());
    }

    @PatchMapping(value = "/admin/reports/{reportId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ReportResponse processReport(
            @PathVariable Integer reportId,
            @RequestBody JsonNode request
    ) {
        return reportService.processReport(reportId, new ReportProcessRequest(statusValue(request, "status")));
    }

    private ReportTargetType targetTypeValue(JsonNode request, String fieldName) {
        String value = textValue(request, fieldName);
        if (value == null) {
            return null;
        }

        try {
            return ReportTargetType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private ReportStatus statusValue(JsonNode request, String fieldName) {
        String value = textValue(request, fieldName);
        if (value == null) {
            return null;
        }

        try {
            return ReportStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
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
}
