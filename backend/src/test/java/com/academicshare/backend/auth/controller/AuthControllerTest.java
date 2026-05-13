package com.academicshare.backend.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.academicshare.backend.auth.session.AuthSessionAttributes;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void signupCreatesUserWithUserRole() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "new-user",
                                  "password": "password123",
                                  "name": "신규 사용자",
                                  "email_address": "new-user@example.com",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.login_id").value("new-user"))
                .andExpect(jsonPath("$.name").value("신규 사용자"))
                .andExpect(jsonPath("$.email_address").value("new-user@example.com"))
                .andExpect(jsonPath("$.deleted_at").value(nullValue()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User savedUser = userRepository.findByLoginId("new-user").orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void signupWithoutRequiredFieldReturns400() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "missing-password",
                                  "name": "사용자",
                                  "email_address": "missing-password@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void signupWithDuplicateLoginIdReturns409() throws Exception {
        saveUser("duplicate-login", "duplicate-login@example.com", "password123");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "duplicate-login",
                                  "password": "password123",
                                  "name": "다른 사용자",
                                  "email_address": "other-login@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void signupWithDuplicateEmailReturns409() throws Exception {
        saveUser("duplicate-email-user", "duplicate-email@example.com", "password123");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "other-email-user",
                                  "password": "password123",
                                  "name": "다른 사용자",
                                  "email_address": "duplicate-email@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void loginCreatesServerSession() throws Exception {
        User user = saveUser("login-user", "login-user@example.com", "password123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "login-user",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.login_id").value("login-user"))
                .andExpect(jsonPath("$.user.deleted_at").value(nullValue()))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn();

        HttpSession session = result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(AuthSessionAttributes.CURRENT_USER_ID)).isEqualTo(user.getId());
    }

    @Test
    void loginRotatesExistingSessionId() throws Exception {
        User user = saveUser("fixed-session-user", "fixed-session@example.com", "password123");
        MockHttpSession existingSession = new MockHttpSession();
        String oldSessionId = existingSession.getId();

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .session(existingSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "fixed-session-user",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        HttpSession session = result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotEqualTo(oldSessionId);
        assertThat(session.getAttribute(AuthSessionAttributes.CURRENT_USER_ID)).isEqualTo(user.getId());
    }

    @Test
    void loginWithoutRequiredFieldReturns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "login-user"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        saveUser("wrong-password-user", "wrong-password@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "wrong-password-user",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));
    }

    @Test
    void deletedUserCannotLogin() throws Exception {
        insertDeletedUser("deleted-login", passwordEncoder.encode("password123"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "deleted-login",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));
    }

    @Test
    void logoutInvalidatesCurrentSession() throws Exception {
        User user = saveUser("logout-user", "logout-user@example.com", "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login_id": "logout-user",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        HttpSession session = loginResult.getRequest().getSession(false);
        assertThat(session.getAttribute(AuthSessionAttributes.CURRENT_USER_ID)).isEqualTo(user.getId());

        mockMvc.perform(post("/auth/logout")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(((org.springframework.mock.web.MockHttpSession) session).isInvalid()).isTrue();
    }

    @Test
    void logoutWithSessionWithoutCsrfReturns403() throws Exception {
        User user = saveUser("csrf-logout-user", "csrf-logout@example.com", "password123");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSessionAttributes.CURRENT_USER_ID, user.getId());

        mockMvc.perform(post("/auth/logout").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));
    }

    @Test
    void logoutWithoutSessionReturns401() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));
    }

    private User saveUser(String loginId, String emailAddress, String rawPassword) {
        return userRepository.saveAndFlush(new User(
                loginId,
                passwordEncoder.encode(rawPassword),
                "사용자",
                emailAddress
        ));
    }

    private void insertDeletedUser(String loginId, String encodedPassword) {
        jdbcTemplate.update("""
                INSERT INTO users (login_id, password, name, email_address, deleted_at, status, role)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'DELETED', 'USER')
                """, loginId, encodedPassword, "탈퇴 사용자", loginId + "@example.com");
    }
}
