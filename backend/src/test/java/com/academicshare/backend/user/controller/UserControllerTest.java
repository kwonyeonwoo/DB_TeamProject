package com.academicshare.backend.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academicshare.backend.auth.session.AuthSessionAttributes;
import com.academicshare.backend.comment.domain.Comment;
import com.academicshare.backend.comment.repository.CommentRepository;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.group.domain.Group;
import com.academicshare.backend.group.domain.GroupMember;
import com.academicshare.backend.group.domain.GroupMemberRole;
import com.academicshare.backend.group.repository.GroupMemberRepository;
import com.academicshare.backend.group.repository.GroupRepository;
import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.schedule.domain.Schedule;
import com.academicshare.backend.schedule.repository.ScheduleRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.domain.UserStatus;
import com.academicshare.backend.user.repository.UserRepository;
import com.academicshare.backend.user.service.UserService;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getMeReturnsCurrentUserAndRequiresAuthentication() throws Exception {
        User user = saveUser("user-get-me", "Get Me", "user-get-me@example.com", "password123");

        mockMvc.perform(get("/users/me")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.login_id").value("user-get-me"))
                .andExpect(jsonPath("$.name").value("Get Me"))
                .andExpect(jsonPath("$.email_address").value("user-get-me@example.com"))
                .andExpect(jsonPath("$.deleted_at").value(nullValue()))
                .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));
    }

    @Test
    void updateMeUpdatesProfileAndPassword() throws Exception {
        User user = saveUser("user-update-me", "Before Name", "user-update-me@example.com", "password123");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "After Name",
                                  "email_address": "user-update-after@example.com"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.login_id").value("user-update-me"))
                .andExpect(jsonPath("$.name").value("After Name"))
                .andExpect(jsonPath("$.email_address").value("user-update-after@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User profileUpdated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(profileUpdated.getName()).isEqualTo("After Name");
        assertThat(profileUpdated.getEmailAddress()).isEqualTo("user-update-after@example.com");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Name Only Change",
                                  "email_address": "user-update-after@example.com"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Name Only Change"))
                .andExpect(jsonPath("$.email_address").value("user-update-after@example.com"));

        profileUpdated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(profileUpdated.getName()).isEqualTo("Name Only Change");
        assertThat(profileUpdated.getEmailAddress()).isEqualTo("user-update-after@example.com");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Name Only Change",
                                  "email_address": "email-only-change@example.com"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Name Only Change"))
                .andExpect(jsonPath("$.email_address").value("email-only-change@example.com"));

        profileUpdated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(profileUpdated.getName()).isEqualTo("Name Only Change");
        assertThat(profileUpdated.getEmailAddress()).isEqualTo("email-only-change@example.com");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Name Only Change",
                                  "email_address": "email-only-change@example.com",
                                  "current_password": "password123",
                                  "new_password": "new-password123"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.password").doesNotExist());

        User passwordUpdated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("new-password123", passwordUpdated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("password123", passwordUpdated.getPassword())).isFalse();
    }

    @Test
    void updateMeRejectsInvalidRequests() throws Exception {
        User user = saveUser("user-invalid-update", "Invalid Update", "user-invalid-update@example.com", "password123");
        saveUser("user-email-owner", "Email Owner", "user-email-owner@example.com", "password123");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Invalid Update\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"new_password\":\"new-password123\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "current_password": "wrong-password",
                                  "new_password": "new-password123"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "current_password": "password123",
                                  "new_password": "password123"
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email_address\":\"user-email-owner@example.com\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, user.getId())
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Unauthenticated\"}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));
    }

    @Test
    void deleteMeMarksDeletedInvalidatesSessionAndKeepsPersonalDataAndContent() throws Exception {
        User user = saveUser("user-delete-me", "Delete Me", "user-delete-me@example.com", "password123");
        Post post = postRepository.saveAndFlush(new Post(
                user.getId(),
                "Deleted user's post",
                "content",
                "Major",
                "Subject",
                false
        ));
        Comment comment = commentRepository.saveAndFlush(new Comment(
                user.getId(),
                post.getId(),
                null,
                "Deleted user's comment",
                false
        ));
        Schedule personalSchedule = saveSchedule(user, null, "Personal schedule");
        Group memberGroup = saveGroup(user, "member-group", "Member Group");
        User memberGroupLeader = saveUser("member-group-leader", "Member Group Leader", "member-group-leader@example.com", "password123");
        saveMembership(memberGroup, memberGroupLeader, GroupMemberRole.LEADER);
        saveMembership(memberGroup, user, GroupMemberRole.MEMBER);
        Group transferGroup = saveGroup(user, "transfer-group", "Transfer Group");
        User earlyMember = saveUser("early-member", "Early Member", "early-member@example.com", "password123");
        User lateMember = saveUser("late-member", "Late Member", "late-member@example.com", "password123");
        saveMembership(transferGroup, user, GroupMemberRole.LEADER);
        saveMembership(transferGroup, lateMember, GroupMemberRole.MEMBER);
        saveMembership(transferGroup, earlyMember, GroupMemberRole.MEMBER);
        setJoinedAt(transferGroup, user, "2026-05-01 09:00:00");
        setJoinedAt(transferGroup, earlyMember, "2026-05-02 09:00:00");
        setJoinedAt(transferGroup, lateMember, "2026-05-03 09:00:00");
        Group soloGroup = saveGroup(user, "solo-group", "Solo Group");
        saveMembership(soloGroup, user, GroupMemberRole.LEADER);
        Schedule soloGroupSchedule = saveSchedule(user, soloGroup, "Solo group schedule");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthSessionAttributes.CURRENT_USER_ID, user.getId());

        mockMvc.perform(delete("/users/me")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();

        User deletedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(deletedUser.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(deletedUser.getDeletedAt()).isNotNull();
        assertThat(deletedUser.getLoginId()).isEqualTo("user-delete-me");
        assertThat(deletedUser.getPassword()).isNotNull();
        assertThat(deletedUser.getName()).isEqualTo("Delete Me");
        assertThat(deletedUser.getEmailAddress()).isEqualTo("user-delete-me@example.com");
        assertThat(scheduleRepository.existsById(personalSchedule.getId())).isFalse();
        assertThat(postRepository.existsById(post.getId())).isTrue();
        assertThat(commentRepository.existsById(comment.getId())).isTrue();
        assertThat(groupMemberRepository.existsByGroupIdAndUserId(memberGroup.getId(), user.getId())).isFalse();
        assertThat(groupMemberRepository.existsByGroupIdAndUserId(transferGroup.getId(), user.getId())).isFalse();
        assertThat(groupRepository.findById(transferGroup.getId()).orElseThrow().getLeaderId()).isEqualTo(earlyMember.getId());
        assertThat(groupMemberRepository.findById(new com.academicshare.backend.group.domain.GroupMemberId(
                transferGroup.getId(),
                earlyMember.getId()
        )).orElseThrow().getRole()).isEqualTo(GroupMemberRole.LEADER);
        assertThat(groupRepository.existsById(soloGroup.getId())).isFalse();
        assertThat(scheduleRepository.existsById(soloGroupSchedule.getId())).isFalse();
    }

    @Test
    void deleteMeRequiresAuthentication() throws Exception {
        mockMvc.perform(delete("/users/me")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));
    }

    @Test
    void clearExpiredDeletedUserPersonalDataClearsOnlyUsersDeletedAtLeastSixMonthsAgo() {
        User expired = saveUser("expired-deleted", "Expired Deleted", "expired-deleted@example.com", "password123");
        User recent = saveUser("recent-deleted", "Recent Deleted", "recent-deleted@example.com", "password123");
        User active = saveUser("active-user", "Active User", "active-user@example.com", "password123");
        jdbcTemplate.update(
                "UPDATE users SET status = 'DELETED', deleted_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf("2025-10-01 00:00:00"),
                expired.getId()
        );
        jdbcTemplate.update(
                "UPDATE users SET status = 'DELETED', deleted_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf("2026-04-01 00:00:00"),
                recent.getId()
        );
        entityManager.clear();

        int clearedCount = userService.clearExpiredDeletedUserPersonalData(LocalDateTime.parse("2026-05-17T00:00:00"));

        assertThat(clearedCount).isEqualTo(1);
        User cleared = userRepository.findById(expired.getId()).orElseThrow();
        assertThat(cleared.getLoginId()).isNull();
        assertThat(cleared.getPassword()).isNull();
        assertThat(cleared.getName()).isNull();
        assertThat(cleared.getEmailAddress()).isNull();
        User retainedRecent = userRepository.findById(recent.getId()).orElseThrow();
        assertThat(retainedRecent.getLoginId()).isEqualTo("recent-deleted");
        User retainedActive = userRepository.findById(active.getId()).orElseThrow();
        assertThat(retainedActive.getLoginId()).isEqualTo("active-user");
    }

    private User saveUser(String loginId, String name, String emailAddress, String rawPassword) {
        return userRepository.saveAndFlush(new User(
                loginId,
                passwordEncoder.encode(rawPassword),
                name,
                emailAddress
        ));
    }

    private Group saveGroup(User leader, String groupCode, String name) {
        return groupRepository.saveAndFlush(new Group(
                groupCode,
                name,
                leader.getId()
        ));
    }

    private GroupMember saveMembership(Group group, User user, GroupMemberRole role) {
        return groupMemberRepository.saveAndFlush(new GroupMember(
                group.getId(),
                user.getId(),
                role
        ));
    }

    private Schedule saveSchedule(User user, Group group, String title) {
        return scheduleRepository.saveAndFlush(new Schedule(
                user.getId(),
                group == null ? null : group.getId(),
                title,
                LocalDateTime.parse("2026-05-12T10:00:00"),
                LocalDateTime.parse("2026-05-12T12:00:00"),
                "schedule description",
                1
        ));
    }

    private void setJoinedAt(Group group, User user, String joinedAt) {
        jdbcTemplate.update(
                "UPDATE group_members SET joined_at = ? WHERE group_id = ? AND user_id = ?",
                java.sql.Timestamp.valueOf(joinedAt),
                group.getId(),
                user.getId()
        );
    }
}
