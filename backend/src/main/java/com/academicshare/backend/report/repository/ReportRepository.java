package com.academicshare.backend.report.repository;

import com.academicshare.backend.report.domain.Report;
import com.academicshare.backend.report.domain.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(
            Integer reporterId,
            ReportTargetType targetType,
            Integer targetId
    );
}
