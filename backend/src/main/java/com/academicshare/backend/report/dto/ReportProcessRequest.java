package com.academicshare.backend.report.dto;

import com.academicshare.backend.report.domain.ReportStatus;

public record ReportProcessRequest(
        ReportStatus status
) {
}
