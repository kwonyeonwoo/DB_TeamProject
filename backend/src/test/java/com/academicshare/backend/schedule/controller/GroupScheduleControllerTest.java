package com.academicshare.backend.schedule.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academicshare.backend.auth.session.AuthSessionAttributes;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.group.domain.Group;
import com.academicshare.backend.group.domain.GroupMember;
import com.academicshare.backend.group.domain.GroupMemberRole;
import com.academicshare.backend.group.repository.GroupMemberRepository;
import com.academicshare.backend.group.repository.GroupRepository;
import com.academicshare.backend.schedule.domain.Schedule;
import com.academicshare.backend.schedule.repository.ScheduleRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GroupScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    void getGroupSchedulesReturnsOnlyGroupSchedulesAndSupportsPeriodFilters() throws Exception {
        User member = saveUser("gcal-list-member", "GCAL List Member", "gcal-list-member@example.com");
        User other = saveUser("gcal-list-other", "GCAL List Other", "gcal-list-other@example.com");
        Group group = saveGroup(member, "gcal-list-group", "GCAL List Group");
        Group otherGroup = saveGroup(other, "gcal-list-other-group", "GCAL Other Group");
        saveMembership(group, member, GroupMemberRole.LEADER);
        saveMembership(otherGroup, other, GroupMemberRole.LEADER);
        Schedule older = saveGroupSchedule(member, group, "older", "2026-05-01T09:00:00", "2026-05-01T10:00:00");
        Schedule overlapping = saveGroupSchedule(other, group, "overlapping", "2026-05-12T10:00:00", "2026-05-12T12:00:00");
        Schedule later = saveGroupSchedule(member, group, "later", "2026-06-01T09:00:00", "2026-06-01T10:00:00");
        saveGroupSchedule(other, otherGroup, "other group", "2026-05-12T10:00:00", "2026-05-12T12:00:00");
        savePersonalSchedule(member, "personal", "2026-05-12T10:00:00", "2026-05-12T12:00:00");

        mockMvc.perform(get("/groups/{groupId}/schedules", group.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[*].id", contains(older.getId(), overlapping.getId(), later.getId())))
                .andExpect(jsonPath("$.items[0].group_id").value(group.getId()))
                .andExpect(jsonPath("$.items[0].updated_at").value(nullValue()));

        mockMvc.perform(get("/groups/{groupId}/schedules", group.getId())
                        .param("start_at", "2026-05-12T11:00:00")
                        .param("end_at", "2026-05-12T13:00:00")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(overlapping.getId()));

        mockMvc.perform(get("/groups/{groupId}/schedules", group.getId())
                        .param("start_at", "2026-05-12T11:00:00")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", contains(overlapping.getId(), later.getId())));

        mockMvc.perform(get("/groups/{groupId}/schedules", group.getId())
                        .param("end_at", "2026-05-02T00:00:00")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", contains(older.getId())));
    }

    @Test
    void getGroupSchedulesRejectsInvalidDateQueryAndUnauthorizedGroupAccess() throws Exception {
        User member = saveUser("gcal-query-member", "GCAL Query Member", "gcal-query-member@example.com");
        User outsider = saveUser("gcal-query-outsider", "GCAL Query Outsider", "gcal-query-outsider@example.com");
        Group group = saveGroup(member, "gcal-query-group", "GCAL Query Group");
        saveMembership(group, member, GroupMemberRole.LEADER);

        mockMvc.perform(get("/groups/{groupId}/schedules", group.getId())
                        .param("start_at", "not-a-date")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(get("/groups/{groupId}/schedules", group.getId())
                        .param("start_at", "2026-05-12T13:00:00")
                        .param("end_at", "2026-05-12T12:00:00")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(get("/groups/{groupId}/schedules", group.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, outsider.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(get("/groups/{groupId}/schedules", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void createGroupScheduleReturnsCreatedScheduleAndRejectsInvalidRequests() throws Exception {
        User member = saveUser("gcal-create-member", "GCAL Create Member", "gcal-create-member@example.com");
        User outsider = saveUser("gcal-create-outsider", "GCAL Create Outsider", "gcal-create-outsider@example.com");
        Group group = saveGroup(member, "gcal-create-group", "GCAL Create Group");
        saveMembership(group, member, GroupMemberRole.LEADER);

        mockMvc.perform(post("/groups/{groupId}/schedules", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Group Midterm",
                                  "start_at": "2026-05-12T10:00:00",
                                  "end_at": "2026-05-12T12:00:00",
                                  "description": "Room 101",
                                  "type": 3
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.user_id").value(member.getId()))
                .andExpect(jsonPath("$.group_id").value(group.getId()))
                .andExpect(jsonPath("$.title").value("Group Midterm"))
                .andExpect(jsonPath("$.start_at").value("2026-05-12T10:00:00"))
                .andExpect(jsonPath("$.end_at").value("2026-05-12T12:00:00"))
                .andExpect(jsonPath("$.description").value("Room 101"))
                .andExpect(jsonPath("$.type").value(3))
                .andExpect(jsonPath("$.created_at").isNotEmpty())
                .andExpect(jsonPath("$.updated_at").value(nullValue()));

        Schedule saved = scheduleRepository.findAll().get(0);
        assertThat(saved.getUserId()).isEqualTo(member.getId());
        assertThat(saved.getGroupId()).isEqualTo(group.getId());

        mockMvc.perform(post("/groups/{groupId}/schedules", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "start_at": "2026-05-12T10:00:00",
                                  "end_at": "2026-05-12T12:00:00",
                                  "type": 3
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/groups/{groupId}/schedules", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid type",
                                  "start_at": "2026-05-12T10:00:00",
                                  "end_at": "2026-05-12T12:00:00",
                                  "type": 6
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/groups/{groupId}/schedules", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid period",
                                  "start_at": "2026-05-12T12:00:00",
                                  "end_at": "2026-05-12T10:00:00",
                                  "type": 1
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/groups/{groupId}/schedules", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Blocked",
                                  "start_at": "2026-05-12T10:00:00",
                                  "end_at": "2026-05-12T12:00:00",
                                  "type": 1
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, outsider.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(post("/groups/{groupId}/schedules", 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Missing group",
                                  "start_at": "2026-05-12T10:00:00",
                                  "end_at": "2026-05-12T12:00:00",
                                  "type": 1
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void updateGroupScheduleRequiresMemberGroupScheduleAndValidValues() throws Exception {
        User member = saveUser("gcal-update-member", "GCAL Update Member", "gcal-update-member@example.com");
        User outsider = saveUser("gcal-update-outsider", "GCAL Update Outsider", "gcal-update-outsider@example.com");
        Group group = saveGroup(member, "gcal-update-group", "GCAL Update Group");
        Group otherGroup = saveGroup(outsider, "gcal-update-other-group", "GCAL Update Other Group");
        saveMembership(group, member, GroupMemberRole.LEADER);
        saveMembership(otherGroup, outsider, GroupMemberRole.LEADER);
        Schedule schedule = saveGroupSchedule(member, group, "Before", "2026-05-12T10:00:00", "2026-05-12T12:00:00");
        Schedule otherGroupSchedule = saveGroupSchedule(outsider, otherGroup, "Other group", "2026-05-13T10:00:00", "2026-05-13T12:00:00");
        Schedule personalSchedule = savePersonalSchedule(member, "Personal", "2026-05-14T10:00:00", "2026-05-14T12:00:00");

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", group.getId(), schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "After",
                                  "end_at": "2026-05-12T13:00:00",
                                  "description": null,
                                  "type": 5
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("After"))
                .andExpect(jsonPath("$.end_at").value("2026-05-12T13:00:00"))
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.type").value(5))
                .andExpect(jsonPath("$.updated_at").isNotEmpty());

        Schedule updated = scheduleRepository.findById(schedule.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("After");
        assertThat(updated.getDescription()).isNull();
        assertThat(updated.getUpdatedAt()).isNotNull();

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", group.getId(), schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":0}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", group.getId(), schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"end_at\":\"2026-05-12T09:00:00\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", group.getId(), schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", group.getId(), schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Blocked\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, outsider.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", 999_999, schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Missing group\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", group.getId(), 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Missing schedule\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", group.getId(), otherGroupSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Wrong group\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", group.getId(), personalSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Personal\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void deleteGroupScheduleRequiresMemberGroupScheduleAndExistingResources() throws Exception {
        User member = saveUser("gcal-delete-member", "GCAL Delete Member", "gcal-delete-member@example.com");
        User outsider = saveUser("gcal-delete-outsider", "GCAL Delete Outsider", "gcal-delete-outsider@example.com");
        Group group = saveGroup(member, "gcal-delete-group", "GCAL Delete Group");
        Group otherGroup = saveGroup(outsider, "gcal-delete-other-group", "GCAL Delete Other Group");
        saveMembership(group, member, GroupMemberRole.LEADER);
        saveMembership(otherGroup, outsider, GroupMemberRole.LEADER);
        Schedule schedule = saveGroupSchedule(member, group, "Delete me", "2026-05-12T10:00:00", "2026-05-12T12:00:00");
        Schedule otherGroupSchedule = saveGroupSchedule(outsider, otherGroup, "Other group", "2026-05-13T10:00:00", "2026-05-13T12:00:00");
        Schedule personalSchedule = savePersonalSchedule(member, "Personal", "2026-05-14T10:00:00", "2026-05-14T12:00:00");

        mockMvc.perform(delete("/groups/{groupId}/schedules/{scheduleId}", group.getId(), otherGroupSchedule.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(delete("/groups/{groupId}/schedules/{scheduleId}", group.getId(), personalSchedule.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(delete("/groups/{groupId}/schedules/{scheduleId}", group.getId(), schedule.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, outsider.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(delete("/groups/{groupId}/schedules/{scheduleId}", 999_999, schedule.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(delete("/groups/{groupId}/schedules/{scheduleId}", group.getId(), 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        assertThat(scheduleRepository.existsById(schedule.getId())).isTrue();

        mockMvc.perform(delete("/groups/{groupId}/schedules/{scheduleId}", group.getId(), schedule.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(scheduleRepository.existsById(schedule.getId())).isFalse();
        assertThat(scheduleRepository.existsById(otherGroupSchedule.getId())).isTrue();
        assertThat(scheduleRepository.existsById(personalSchedule.getId())).isTrue();
    }

    @Test
    void groupScheduleApiRequiresAuthentication() throws Exception {
        User member = saveUser("gcal-auth-member", "GCAL Auth Member", "gcal-auth-member@example.com");
        Group group = saveGroup(member, "gcal-auth-group", "GCAL Auth Group");
        Schedule schedule = saveGroupSchedule(member, group, "Auth schedule", "2026-05-12T10:00:00", "2026-05-12T12:00:00");

        mockMvc.perform(get("/groups/{groupId}/schedules", group.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(post("/groups/{groupId}/schedules", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Auth create",
                                  "start_at": "2026-05-12T10:00:00",
                                  "end_at": "2026-05-12T12:00:00",
                                  "type": 1
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(patch("/groups/{groupId}/schedules/{scheduleId}", group.getId(), schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Auth update\"}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(delete("/groups/{groupId}/schedules/{scheduleId}", group.getId(), schedule.getId())
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

    private Schedule savePersonalSchedule(User user, String title, String startAt, String endAt) {
        return scheduleRepository.saveAndFlush(new Schedule(
                user.getId(),
                null,
                title,
                LocalDateTime.parse(startAt),
                LocalDateTime.parse(endAt),
                "personal schedule description",
                1
        ));
    }

    private Schedule saveGroupSchedule(User user, Group group, String title, String startAt, String endAt) {
        return scheduleRepository.saveAndFlush(new Schedule(
                user.getId(),
                group.getId(),
                title,
                LocalDateTime.parse(startAt),
                LocalDateTime.parse(endAt),
                "group schedule description",
                1
        ));
    }
}
