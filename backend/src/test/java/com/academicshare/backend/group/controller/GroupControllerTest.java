package com.academicshare.backend.group.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
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
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Test
    void getMyGroupsReturnsOnlyGroupsJoinedByCurrentUser() throws Exception {
        User currentUser = saveUser("group-list-current", "Group List Current", "group-list-current@example.com");
        User otherUser = saveUser("group-list-other", "Group List Other", "group-list-other@example.com");
        Group firstGroup = saveGroup(currentUser, "group-list-first", "First Group");
        Group secondGroup = saveGroup(otherUser, "group-list-second", "Second Group");
        Group otherGroup = saveGroup(otherUser, "group-list-other-only", "Other Group");
        saveMembership(firstGroup, currentUser, GroupMemberRole.LEADER);
        saveMembership(secondGroup, currentUser, GroupMemberRole.MEMBER);
        saveMembership(otherGroup, otherUser, GroupMemberRole.LEADER);

        mockMvc.perform(get("/groups")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[*].id", contains(firstGroup.getId(), secondGroup.getId())))
                .andExpect(jsonPath("$.items[0].group_code").value("group-list-first"))
                .andExpect(jsonPath("$.items[0].name").value("First Group"))
                .andExpect(jsonPath("$.items[0].leader_id").value(currentUser.getId()))
                .andExpect(jsonPath("$.items[0].created_at").isNotEmpty());
    }

    @Test
    void createGroupReturnsGroupAndLeaderMembership() throws Exception {
        User currentUser = saveUser("group-create-current", "Group Create Current", "group-create-current@example.com");

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Study Team\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.group.id").isNumber())
                .andExpect(jsonPath("$.group.group_code", not(blankOrNullString())))
                .andExpect(jsonPath("$.group.name").value("Study Team"))
                .andExpect(jsonPath("$.group.leader_id").value(currentUser.getId()))
                .andExpect(jsonPath("$.group.created_at").isNotEmpty())
                .andExpect(jsonPath("$.membership.group_id").isNumber())
                .andExpect(jsonPath("$.membership.user_id").value(currentUser.getId()))
                .andExpect(jsonPath("$.membership.role").value(GroupMemberRole.LEADER.name()))
                .andExpect(jsonPath("$.membership.joined_at").isNotEmpty());

        Group group = groupRepository.findAll().get(0);
        assertThat(group.getName()).isEqualTo("Study Team");
        assertThat(group.getLeaderId()).isEqualTo(currentUser.getId());
        assertThat(group.getGroupCode()).isNotBlank();
        assertThat(groupMemberRepository.existsByGroupIdAndUserId(group.getId(), currentUser.getId())).isTrue();
    }

    @Test
    void createGroupRejectsMissingOrInvalidName() throws Exception {
        User currentUser = saveUser("group-create-invalid", "Group Create Invalid", "group-create-invalid@example.com");
        String tooLongName = "a".repeat(101);

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(tooLongName))
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void joinGroupByCodeReturnsMemberMembershipAndRejectsDuplicateOrInvalidCode() throws Exception {
        User leader = saveUser("group-join-leader", "Group Join Leader", "group-join-leader@example.com");
        User currentUser = saveUser("group-join-current", "Group Join Current", "group-join-current@example.com");
        Group group = saveGroup(leader, "join-code", "Join Group");
        saveMembership(group, leader, GroupMemberRole.LEADER);

        mockMvc.perform(post("/groups/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"group_code\":\"join-code\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.group_id").value(group.getId()))
                .andExpect(jsonPath("$.user_id").value(currentUser.getId()))
                .andExpect(jsonPath("$.role").value(GroupMemberRole.MEMBER.name()))
                .andExpect(jsonPath("$.joined_at").isNotEmpty());

        assertThat(groupMemberRepository.existsByGroupIdAndUserId(group.getId(), currentUser.getId())).isTrue();

        mockMvc.perform(post("/groups/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"group_code\":\"join-code\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()));

        mockMvc.perform(post("/groups/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"group_code\":\"missing-code\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void getGroupDetailReturnsGroupAndMembersOnlyForMembers() throws Exception {
        User leader = saveUser("group-detail-leader", "Group Detail Leader", "group-detail-leader@example.com");
        User member = saveUser("group-detail-member", "Group Detail Member", "group-detail-member@example.com");
        User outsider = saveUser("group-detail-outsider", "Group Detail Outsider", "group-detail-outsider@example.com");
        Group group = saveGroup(leader, "detail-code", "Detail Group");
        saveMembership(group, leader, GroupMemberRole.LEADER);
        saveMembership(group, member, GroupMemberRole.MEMBER);

        mockMvc.perform(get("/groups/{groupId}", group.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.group.id").value(group.getId()))
                .andExpect(jsonPath("$.group.group_code").value("detail-code"))
                .andExpect(jsonPath("$.group.name").value("Detail Group"))
                .andExpect(jsonPath("$.members.length()").value(2))
                .andExpect(jsonPath("$.members[0].user_id").value(leader.getId()))
                .andExpect(jsonPath("$.members[0].role").value(GroupMemberRole.LEADER.name()))
                .andExpect(jsonPath("$.members[1].user_id").value(member.getId()))
                .andExpect(jsonPath("$.members[1].role").value(GroupMemberRole.MEMBER.name()));

        mockMvc.perform(get("/groups/{groupId}", group.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, outsider.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(get("/groups/{groupId}", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, member.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void groupApiRequiresAuthentication() throws Exception {
        User leader = saveUser("group-auth-leader", "Group Auth Leader", "group-auth-leader@example.com");
        Group group = saveGroup(leader, "auth-code", "Auth Group");

        mockMvc.perform(get("/groups"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Auth Group\"}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(post("/groups/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"group_code\":\"auth-code\"}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()));

        mockMvc.perform(get("/groups/{groupId}", group.getId()))
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
}
