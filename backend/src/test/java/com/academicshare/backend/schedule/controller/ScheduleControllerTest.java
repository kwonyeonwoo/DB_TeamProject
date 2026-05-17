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
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    void getMySchedulesReturnsOnlyCurrentUsersPersonalSchedulesAndSupportsPeriodFilters() throws Exception {
        User currentUser = saveUser("schedule-list-current", "Schedule List Current", "schedule-list-current@example.com");
        User otherUser = saveUser("schedule-list-other", "Schedule List Other", "schedule-list-other@example.com");
        Schedule older = saveSchedule(currentUser, "older", "2026-05-01T09:00:00", "2026-05-01T10:00:00");
        Schedule overlapping = saveSchedule(currentUser, "overlapping", "2026-05-12T10:00:00", "2026-05-12T12:00:00");
        Schedule later = saveSchedule(currentUser, "later", "2026-06-01T09:00:00", "2026-06-01T10:00:00");
        saveSchedule(otherUser, "other", "2026-05-12T10:00:00", "2026-05-12T12:00:00");
        saveGroupSchedule(currentUser, "group schedule", "2026-05-12T10:00:00", "2026-05-12T12:00:00");

        mockMvc.perform(get("/me/schedules")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[*].id", contains(older.getId(), overlapping.getId(), later.getId())))
                .andExpect(jsonPath("$.items[0].group_id").value(nullValue()))
                .andExpect(jsonPath("$.items[0].updated_at").value(nullValue()));

        mockMvc.perform(get("/me/schedules")
                        .param("start_at", "2026-05-12T11:00:00")
                        .param("end_at", "2026-05-12T13:00:00")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(overlapping.getId()));

        mockMvc.perform(get("/me/schedules")
                        .param("start_at", "2026-05-12T11:00:00")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", contains(overlapping.getId(), later.getId())));

        mockMvc.perform(get("/me/schedules")
                        .param("end_at", "2026-05-02T00:00:00")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", contains(older.getId())));
    }

    @Test
    void getMySchedulesRejectsInvalidDateQuery() throws Exception {
        User currentUser = saveUser("schedule-query-invalid", "Schedule Query Invalid", "schedule-query-invalid@example.com");

        mockMvc.perform(get("/me/schedules")
                        .param("start_at", "not-a-date")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(get("/me/schedules")
                        .param("start_at", "2026-05-12T13:00:00")
                        .param("end_at", "2026-05-12T12:00:00")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void createMyScheduleReturnsCreatedScheduleAndRejectsInvalidRequests() throws Exception {
        User currentUser = saveUser("schedule-create-current", "Schedule Create Current", "schedule-create-current@example.com");

        mockMvc.perform(post("/me/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Midterm",
                                  "start_at": "2026-05-12T10:00:00",
                                  "end_at": "2026-05-12T12:00:00",
                                  "description": "Room 101",
                                  "type": 3
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.user_id").value(currentUser.getId()))
                .andExpect(jsonPath("$.group_id").value(nullValue()))
                .andExpect(jsonPath("$.title").value("Midterm"))
                .andExpect(jsonPath("$.start_at").value("2026-05-12T10:00:00"))
                .andExpect(jsonPath("$.end_at").value("2026-05-12T12:00:00"))
                .andExpect(jsonPath("$.description").value("Room 101"))
                .andExpect(jsonPath("$.type").value(3))
                .andExpect(jsonPath("$.created_at").isNotEmpty())
                .andExpect(jsonPath("$.updated_at").value(nullValue()));

        Schedule saved = scheduleRepository.findAll().get(0);
        assertThat(saved.getUserId()).isEqualTo(currentUser.getId());
        assertThat(saved.getGroupId()).isNull();

        mockMvc.perform(post("/me/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "start_at": "2026-05-12T10:00:00",
                                  "end_at": "2026-05-12T12:00:00",
                                  "type": 3
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/me/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid type",
                                  "start_at": "2026-05-12T10:00:00",
                                  "end_at": "2026-05-12T12:00:00",
                                  "type": 6
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/me/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid period",
                                  "start_at": "2026-05-12T12:00:00",
                                  "end_at": "2026-05-12T10:00:00",
                                  "type": 1
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void updateMyScheduleRequiresOwnerAndValidValues() throws Exception {
        User owner = saveUser("schedule-update-owner", "Schedule Update Owner", "schedule-update-owner@example.com");
        User other = saveUser("schedule-update-other", "Schedule Update Other", "schedule-update-other@example.com");
        Schedule schedule = saveSchedule(owner, "Before", "2026-05-12T10:00:00", "2026-05-12T12:00:00");
        Schedule ownerGroupSchedule = saveGroupSchedule(owner, "Owner group update", "2026-05-14T10:00:00", "2026-05-14T12:00:00");

        mockMvc.perform(patch("/me/schedules/{scheduleId}", schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "After",
                                  "end_at": "2026-05-12T13:00:00",
                                  "description": null,
                                  "type": 5
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
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

        mockMvc.perform(patch("/me/schedules/{scheduleId}", schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":0}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/me/schedules/{scheduleId}", schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"end_at\":\"2026-05-12T09:00:00\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/me/schedules/{scheduleId}", schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/me/schedules/{scheduleId}", schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Blocked\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, other.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(patch("/me/schedules/{scheduleId}", ownerGroupSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Blocked group\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        Schedule unchangedGroupSchedule = scheduleRepository.findById(ownerGroupSchedule.getId()).orElseThrow();
        assertThat(unchangedGroupSchedule.getTitle()).isEqualTo("Owner group update");
        assertThat(unchangedGroupSchedule.getUpdatedAt()).isNull();

        mockMvc.perform(patch("/me/schedules/{scheduleId}", 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Missing\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void deleteMyScheduleRequiresOwnerAndExistingSchedule() throws Exception {
        User owner = saveUser("schedule-delete-owner", "Schedule Delete Owner", "schedule-delete-owner@example.com");
        User other = saveUser("schedule-delete-other", "Schedule Delete Other", "schedule-delete-other@example.com");
        Schedule ownerSchedule = saveSchedule(owner, "Delete me", "2026-05-12T10:00:00", "2026-05-12T12:00:00");
        Schedule otherSchedule = saveSchedule(other, "Other schedule", "2026-05-13T10:00:00", "2026-05-13T12:00:00");
        Schedule ownerGroupSchedule = saveGroupSchedule(owner, "Owner group delete", "2026-05-14T10:00:00", "2026-05-14T12:00:00");

        mockMvc.perform(delete("/me/schedules/{scheduleId}", otherSchedule.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(delete("/me/schedules/{scheduleId}", ownerGroupSchedule.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        assertThat(scheduleRepository.existsById(ownerGroupSchedule.getId())).isTrue();

        mockMvc.perform(delete("/me/schedules/{scheduleId}", ownerSchedule.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(scheduleRepository.existsById(ownerSchedule.getId())).isFalse();
        assertThat(scheduleRepository.existsById(otherSchedule.getId())).isTrue();

        mockMvc.perform(delete("/me/schedules/{scheduleId}", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void scheduleApiRequiresAuthentication() throws Exception {
        User owner = saveUser("schedule-auth-owner", "Schedule Auth Owner", "schedule-auth-owner@example.com");
        Schedule schedule = saveSchedule(owner, "Auth schedule", "2026-05-12T10:00:00", "2026-05-12T12:00:00");

        mockMvc.perform(get("/me/schedules"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(post("/me/schedules")
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

        mockMvc.perform(patch("/me/schedules/{scheduleId}", schedule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Auth update\"}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(delete("/me/schedules/{scheduleId}", schedule.getId())
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

    private Schedule saveSchedule(User user, String title, String startAt, String endAt) {
        return scheduleRepository.saveAndFlush(new Schedule(
                user.getId(),
                null,
                title,
                LocalDateTime.parse(startAt),
                LocalDateTime.parse(endAt),
                "schedule description",
                1
        ));
    }

    private Schedule saveGroupSchedule(User user, String title, String startAt, String endAt) {
        Group group = groupRepository.saveAndFlush(new Group(
                "group-" + title.replace(" ", "-"),
                "Group " + title,
                user.getId()
        ));
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
