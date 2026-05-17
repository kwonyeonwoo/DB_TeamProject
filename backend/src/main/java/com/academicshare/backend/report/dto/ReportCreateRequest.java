package com.academicshare.backend.report.dto;

import com.academicshare.backend.report.domain.ReportTargetType;

public record ReportCreateRequest(
        ReportTargetType targetType,
        Integer targetId,
        Integer reasonType
) {
}
