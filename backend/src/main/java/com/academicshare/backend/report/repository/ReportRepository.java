package com.academicshare.backend.report.repository;

import com.academicshare.backend.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Integer> {
}
