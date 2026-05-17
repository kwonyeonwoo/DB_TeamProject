package com.academicshare.backend.report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "report",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_report_target",
                columnNames = {"reporter_id", "target_type", "target_id"}
        )
)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "reporter_id", nullable = false)
    private Integer reporterId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @NotNull
    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @NotNull
    @Min(1)
    @Max(4)
    @Column(name = "reason_type", nullable = false)
    private Integer reasonType;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "processed_by")
    private Integer processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    protected Report() {
    }

    public Report(Integer reporterId, ReportTargetType targetType, Integer targetId, Integer reasonType) {
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reasonType = reasonType;
        this.status = ReportStatus.PENDING;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ReportStatus.PENDING;
        }
    }

    public Integer getId() {
        return id;
    }

    public Integer getReporterId() {
        return reporterId;
    }

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public Integer getReasonType() {
        return reasonType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void process(Integer adminId) {
        status = ReportStatus.PROCESSED;
        processedBy = adminId;
        processedAt = LocalDateTime.now();
    }
}
