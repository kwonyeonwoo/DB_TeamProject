package com.academicshare.backend.report.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academicshare.backend.auth.session.AuthSessionAttributes;
import com.academicshare.backend.comment.domain.Comment;
import com.academicshare.backend.comment.repository.CommentRepository;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.report.domain.Report;
import com.academicshare.backend.report.domain.ReportStatus;
import com.academicshare.backend.report.domain.ReportTargetType;
import com.academicshare.backend.report.repository.ReportRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportControllerTest {

    private static final String DELETED_TARGET = "\uC0AD\uC81C\uB41C \uB300\uC0C1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createReportAsUserReturnsPendingReport() throws Exception {
        User reporter = saveUser("report-create-user", "Report User", "report-create-user@example.com");
        User author = saveUser("report-create-author", "Report Author", "report-create-author@example.com");
        Post post = savePost(author, "Report target post");

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "POST",
                                  "target_id": %d,
                                  "reason_type": 1
                                }
                                """.formatted(post.getId()))
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.reporter_id").value(reporter.getId()))
                .andExpect(jsonPath("$.target_type").value("POST"))
                .andExpect(jsonPath("$.target_id").value(post.getId()))
                .andExpect(jsonPath("$.target_display_name").value("Report target post"))
                .andExpect(jsonPath("$.reason_type").value(1))
                .andExpect(jsonPath("$.created_at").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.processed_by").value(nullValue()))
                .andExpect(jsonPath("$.processed_at").value(nullValue()));

        assertThat(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporter.getId(),
                ReportTargetType.POST,
                post.getId()
        )).isTrue();
    }

    @Test
    void createReportSupportsCommentTarget() throws Exception {
        User reporter = saveUser("report-comment-user", "Report Comment User", "report-comment-user@example.com");
        User author = saveUser("report-comment-author", "Report Comment Author", "report-comment-author@example.com");
        Post post = savePost(author, "Comment target post");
        Comment comment = saveComment(author, post, "Comment target content");

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "COMMENT",
                                  "target_id": %d,
                                  "reason_type": 4
                                }
                                """.formatted(comment.getId()))
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.target_type").value("COMMENT"))
                .andExpect(jsonPath("$.target_id").value(comment.getId()))
                .andExpect(jsonPath("$.target_display_name").value("Comment target content"))
                .andExpect(jsonPath("$.reason_type").value(4));
    }

    @Test
    void createReportRejectsMissingOrInvalidValues() throws Exception {
        User reporter = saveUser("report-invalid-user", "Report Invalid User", "report-invalid-user@example.com");
        User author = saveUser("report-invalid-author", "Report Invalid Author", "report-invalid-author@example.com");
        Post post = savePost(author, "Invalid target post");

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_id": %d,
                                  "reason_type": 1
                                }
                                """.formatted(post.getId()))
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "POST",
                                  "reason_type": 1
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "POST",
                                  "target_id": %d
                                }
                                """.formatted(post.getId()))
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "USER",
                                  "target_id": %d,
                                  "reason_type": 1
                                }
                                """.formatted(post.getId()))
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "POST",
                                  "target_id": %d,
                                  "reason_type": 5
                                }
                                """.formatted(post.getId()))
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void createReportRejectsAdminDuplicateAndMissingTarget() throws Exception {
        User reporter = saveUser("report-duplicate-user", "Report Duplicate User", "report-duplicate-user@example.com");
        User author = saveUser("report-duplicate-author", "Report Duplicate Author", "report-duplicate-author@example.com");
        Integer adminId = insertAdmin("report-create-admin");
        Post post = savePost(author, "Duplicate target post");
        reportRepository.saveAndFlush(new Report(reporter.getId(), ReportTargetType.POST, post.getId(), 2));

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "POST",
                                  "target_id": %d,
                                  "reason_type": 1
                                }
                                """.formatted(post.getId()))
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, adminId)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "POST",
                                  "target_id": %d,
                                  "reason_type": 2
                                }
                                """.formatted(post.getId()))
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()));

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "POST",
                                  "target_id": 999999,
                                  "reason_type": 1
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void adminCanListReportsAndDeletedTargetsUseDeletedDisplayName() throws Exception {
        User reporter = saveUser("report-list-user", "Report List User", "report-list-user@example.com");
        User author = saveUser("report-list-author", "Report List Author", "report-list-author@example.com");
        Integer adminId = insertAdmin("report-list-admin");
        Post post = savePost(author, "List deleted post");
        Comment comment = saveComment(author, post, "List deleted comment");
        Report postReport = reportRepository.saveAndFlush(new Report(
                reporter.getId(),
                ReportTargetType.POST,
                post.getId(),
                1
        ));
        Report commentReport = reportRepository.saveAndFlush(new Report(
                reporter.getId(),
                ReportTargetType.COMMENT,
                comment.getId(),
                2
        ));
        commentRepository.delete(comment);
        commentRepository.flush();
        postRepository.delete(post);
        postRepository.flush();

        mockMvc.perform(get("/admin/reports")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[?(@.id == %d)].reporter_id".formatted(postReport.getId())).value(contains(reporter.getId())))
                .andExpect(jsonPath("$.items[?(@.id == %d)].target_display_name".formatted(postReport.getId())).value(contains(DELETED_TARGET)))
                .andExpect(jsonPath("$.items[?(@.id == %d)].processed_by".formatted(postReport.getId())).value(contains(nullValue())))
                .andExpect(jsonPath("$.items[?(@.id == %d)].processed_at".formatted(postReport.getId())).value(contains(nullValue())))
                .andExpect(jsonPath("$.items[?(@.id == %d)].target_type".formatted(commentReport.getId())).value(contains("COMMENT")))
                .andExpect(jsonPath("$.items[?(@.id == %d)].target_display_name".formatted(commentReport.getId())).value(contains(DELETED_TARGET)));
    }

    @Test
    void userCannotListReports() throws Exception {
        User user = saveUser("report-list-forbidden", "Report List Forbidden", "report-list-forbidden@example.com");

        mockMvc.perform(get("/admin/reports")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));
    }

    @Test
    void adminCanProcessReportWithoutDeletingTarget() throws Exception {
        User reporter = saveUser("report-process-user", "Report Process User", "report-process-user@example.com");
        User author = saveUser("report-process-author", "Report Process Author", "report-process-author@example.com");
        Integer adminId = insertAdmin("report-process-admin");
        Post post = savePost(author, "Process target post");
        Report report = reportRepository.saveAndFlush(new Report(
                reporter.getId(),
                ReportTargetType.POST,
                post.getId(),
                3
        ));

        mockMvc.perform(patch("/admin/reports/{reportId}", report.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PROCESSED"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, adminId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(report.getId()))
                .andExpect(jsonPath("$.status").value("PROCESSED"))
                .andExpect(jsonPath("$.processed_by").value(adminId))
                .andExpect(jsonPath("$.processed_at").isNotEmpty())
                .andExpect(jsonPath("$.target_display_name").value("Process target post"));

        Report processedReport = reportRepository.findById(report.getId()).orElseThrow();
        assertThat(processedReport.getStatus()).isEqualTo(ReportStatus.PROCESSED);
        assertThat(processedReport.getProcessedBy()).isEqualTo(adminId);
        assertThat(processedReport.getProcessedAt()).isNotNull();
        assertThat(postRepository.existsById(post.getId())).isTrue();
    }

    @Test
    void processReportRejectsInvalidStatusMissingReportAndUserRole() throws Exception {
        User reporter = saveUser("report-process-invalid-user", "Report Process Invalid User", "report-process-invalid-user@example.com");
        User author = saveUser("report-process-invalid-author", "Report Process Invalid Author", "report-process-invalid-author@example.com");
        Integer adminId = insertAdmin("report-process-invalid-admin");
        Post post = savePost(author, "Process invalid target post");
        Report report = reportRepository.saveAndFlush(new Report(
                reporter.getId(),
                ReportTargetType.POST,
                post.getId(),
                1
        ));

        mockMvc.perform(patch("/admin/reports/{reportId}", report.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PENDING"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, adminId)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/admin/reports/{reportId}", report.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, adminId)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/admin/reports/{reportId}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PROCESSED"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, adminId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(patch("/admin/reports/{reportId}", report.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PROCESSED"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, reporter.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));
    }

    @Test
    void reportApiRequiresAuthentication() throws Exception {
        User author = saveUser("report-auth-author", "Report Auth Author", "report-auth-author@example.com");
        User reporter = saveUser("report-auth-reporter", "Report Auth Reporter", "report-auth-reporter@example.com");
        Post post = savePost(author, "Authentication target post");
        Report report = reportRepository.saveAndFlush(new Report(
                reporter.getId(),
                ReportTargetType.POST,
                post.getId(),
                1
        ));

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_type": "POST",
                                  "target_id": %d,
                                  "reason_type": 1
                                }
                                """.formatted(post.getId()))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(patch("/admin/reports/{reportId}", report.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PROCESSED"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));
    }

    private User saveUser(String loginId, String name, String emailAddress) {
        return userRepository.saveAndFlush(new User(
                loginId,
                "encoded-password",
                name,
                emailAddress
        ));
    }

    private Integer insertAdmin(String loginId) {
        jdbcTemplate.update("""
                INSERT INTO users (login_id, password, name, email_address, status, role)
                VALUES (?, ?, ?, ?, 'ACTIVE', 'ADMIN')
                """, loginId, "encoded-password", "Admin User", loginId + "@example.com");
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login_id = ?", Integer.class, loginId);
    }

    private Post savePost(User user, String title) {
        return postRepository.saveAndFlush(new Post(
                user.getId(),
                title,
                "post content",
                "Major",
                "Subject",
                false
        ));
    }

    private Comment saveComment(User user, Post post, String content) {
        return commentRepository.saveAndFlush(new Comment(
                user.getId(),
                post.getId(),
                null,
                content,
                false
        ));
    }
}
