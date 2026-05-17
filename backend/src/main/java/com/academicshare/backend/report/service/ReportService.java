package com.academicshare.backend.report.service;

import com.academicshare.backend.auth.session.CurrentUserProvider;
import com.academicshare.backend.comment.repository.CommentRepository;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.report.domain.Report;
import com.academicshare.backend.report.domain.ReportStatus;
import com.academicshare.backend.report.domain.ReportTargetType;
import com.academicshare.backend.report.dto.ReportCreateRequest;
import com.academicshare.backend.report.dto.ReportProcessRequest;
import com.academicshare.backend.report.dto.ReportResponse;
import com.academicshare.backend.report.repository.ReportRepository;
import com.academicshare.backend.user.domain.UserRole;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private static final String DELETED_TARGET_DISPLAY_NAME = "\uC0AD\uC81C\uB41C \uB300\uC0C1";

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReportService(
            ReportRepository reportRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public ReportResponse createReport(ReportCreateRequest request) {
        currentUserProvider.requireRole(UserRole.USER);
        validateCreateRequest(request);
        requireTargetExists(request.targetType(), request.targetId());

        Integer reporterId = currentUserProvider.getCurrentUserId();
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporterId,
                request.targetType(),
                request.targetId()
        )) {
            throw new ApiException(ErrorCode.CONFLICT);
        }

        try {
            Report report = reportRepository.saveAndFlush(new Report(
                    reporterId,
                    request.targetType(),
                    request.targetId(),
                    request.reasonType()
            ));
            return toResponse(report);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT);
        }
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReports() {
        currentUserProvider.requireRole(UserRole.ADMIN);

        return reportRepository.findAll(Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                ))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReportResponse processReport(Integer reportId, ReportProcessRequest request) {
        currentUserProvider.requireRole(UserRole.ADMIN);
        validateProcessRequest(request);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        report.process(currentUserProvider.getCurrentUserId());

        return toResponse(report);
    }

    private void validateCreateRequest(ReportCreateRequest request) {
        if (request.targetType() == null || request.targetId() == null || request.reasonType() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.reasonType() < 1 || request.reasonType() > 4) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateProcessRequest(ReportProcessRequest request) {
        if (request.status() != ReportStatus.PROCESSED) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void requireTargetExists(ReportTargetType targetType, Integer targetId) {
        boolean exists = switch (targetType) {
            case POST -> postRepository.existsById(targetId);
            case COMMENT -> commentRepository.existsById(targetId);
        };

        if (!exists) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    private ReportResponse toResponse(Report report) {
        return ReportResponse.from(report, targetDisplayName(report));
    }

    private String targetDisplayName(Report report) {
        return switch (report.getTargetType()) {
            case POST -> postRepository.findById(report.getTargetId())
                    .map(post -> post.getTitle())
                    .orElse(DELETED_TARGET_DISPLAY_NAME);
            case COMMENT -> commentRepository.findById(report.getTargetId())
                    .map(comment -> comment.getContent())
                    .orElse(DELETED_TARGET_DISPLAY_NAME);
        };
    }
}
