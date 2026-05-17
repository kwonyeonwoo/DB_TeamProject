package com.academicshare.backend.report.dto;

import com.academicshare.backend.report.domain.Report;
import com.academicshare.backend.report.domain.ReportStatus;
import com.academicshare.backend.report.domain.ReportTargetType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ReportResponse(
        Integer id,
        Integer reporterId,
        ReportTargetType targetType,
        Integer targetId,
        String targetDisplayName,
        Integer reasonType,
        LocalDateTime createdAt,
        ReportStatus status,
        Integer processedBy,
        LocalDateTime processedAt
) {

    public static ReportResponse from(Report report, String targetDisplayName) {
        return new ReportResponse(
                report.getId(),
                report.getReporterId(),
                report.getTargetType(),
                report.getTargetId(),
                targetDisplayName,
                report.getReasonType(),
                report.getCreatedAt(),
                report.getStatus(),
                report.getProcessedBy(),
                report.getProcessedAt()
        );
    }
}
