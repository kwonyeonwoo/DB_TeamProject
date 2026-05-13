package com.academicshare.backend.auth.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(AuthInfrastructureTestController.class)
class SessionAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void publicLoginPathDoesNotRequireSession() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void publicSignupPathDoesNotRequireSession() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void unknownFutureApiPathWithoutSessionReturns401ByDefault() throws Exception {
        mockMvc.perform(get("/future-api"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));
    }

    @Test
    void protectedPathWithoutSessionReturns401() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void protectedPathWithActiveUserSessionSetsCurrentUser() throws Exception {
        User user = saveUser("session-user", "session-user@example.com");

        mockMvc.perform(get("/users/me")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void protectedPathWithMissingUserSessionReturns401() throws Exception {
        mockMvc.perform(get("/users/me")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, 999999))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));
    }

    @Test
    void protectedPathWithDeletedUserSessionReturns403AndInvalidatesSession() throws Exception {
        Integer deletedUserId = insertDeletedUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSessionAttributes.CURRENT_USER_ID, deletedUserId);

        mockMvc.perform(get("/users/me").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void currentUserRoleHelperAllowsAdmin() throws Exception {
        Integer adminId = insertAdminUser();

        mockMvc.perform(get("/admin/reports")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0]").value("report"));
    }

    @Test
    void currentUserRoleHelperRejectsNonAdmin() throws Exception {
        User user = saveUser("report-user", "report-user@example.com");

        mockMvc.perform(get("/admin/reports")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));
    }

    private User saveUser(String loginId, String emailAddress) {
        return userRepository.saveAndFlush(new User(
                loginId,
                "encoded-password",
                "사용자",
                emailAddress
        ));
    }

    private Integer insertDeletedUser() {
        jdbcTemplate.update("""
                INSERT INTO users (login_id, password, name, email_address, deleted_at, status, role)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'DELETED', 'USER')
                """, "deleted-user", "encoded-password", "탈퇴 사용자", "deleted@example.com");

        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login_id = ?", Integer.class, "deleted-user");
    }

    private Integer insertAdminUser() {
        jdbcTemplate.update("""
                INSERT INTO users (login_id, password, name, email_address, status, role)
                VALUES (?, ?, ?, ?, 'ACTIVE', 'ADMIN')
                """, "admin-user", "encoded-password", "관리자", "admin@example.com");

        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login_id = ?", Integer.class, "admin-user");
    }
}
