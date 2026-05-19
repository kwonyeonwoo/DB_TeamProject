package com.academicshare.backend.group.service;

import com.academicshare.backend.auth.session.CurrentUserProvider;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.group.domain.Group;
import com.academicshare.backend.group.domain.GroupMember;
import com.academicshare.backend.group.domain.GroupMemberRole;
import com.academicshare.backend.group.dto.GroupCreateRequest;
import com.academicshare.backend.group.dto.GroupCreateResponse;
import com.academicshare.backend.group.dto.GroupDetailResponse;
import com.academicshare.backend.group.dto.GroupJoinRequest;
import com.academicshare.backend.group.dto.GroupMemberResponse;
import com.academicshare.backend.group.dto.GroupResponse;
import com.academicshare.backend.group.repository.GroupMemberRepository;
import com.academicshare.backend.group.repository.GroupRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GroupService {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_GROUP_CODE_LENGTH = 255;
    private static final int GROUP_CODE_GENERATION_ATTEMPTS = 5;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public GroupService(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups() {
        return groupRepository.findJoinedGroupsByUserId(currentUserProvider.getCurrentUserId())
                .stream()
                .map(GroupResponse::from)
                .toList();
    }

    @Transactional
    public GroupCreateResponse createGroup(GroupCreateRequest request) {
        validateName(request.name());

        User currentUser = getCurrentUser();
        Integer currentUserId = currentUser.getId();
        Group group = groupRepository.saveAndFlush(new Group(
                generateGroupCode(),
                request.name(),
                currentUserId
        ));
        GroupMember membership = groupMemberRepository.saveAndFlush(new GroupMember(
                group.getId(),
                currentUserId,
                GroupMemberRole.LEADER
        ));

        return GroupCreateResponse.from(group, membership, currentUser.getName());
    }

    @Transactional
    public GroupMemberResponse joinGroup(GroupJoinRequest request) {
        Group group = findGroupByCode(request.groupCode());
        User currentUser = getCurrentUser();
        Integer currentUserId = currentUser.getId();

        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), currentUserId)) {
            throw new ApiException(ErrorCode.CONFLICT);
        }

        try {
            GroupMember membership = groupMemberRepository.saveAndFlush(new GroupMember(
                    group.getId(),
                    currentUserId,
                    GroupMemberRole.MEMBER
            ));
            return GroupMemberResponse.from(membership, currentUser.getName());
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT);
        }
    }

    @Transactional(readOnly = true)
    public GroupDetailResponse getGroupDetail(Integer groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUserProvider.getCurrentUserId())) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }

        List<GroupMember> members = groupMemberRepository.findByGroupIdOrderByJoinedAtAscUserIdAsc(groupId);
        return GroupDetailResponse.from(
                group,
                members,
                findUserNamesById(members)
        );
    }

    private User getCurrentUser() {
        return userRepository.findById(currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.AUTHENTICATION_REQUIRED));
    }

    private Map<Integer, String> findUserNamesById(List<GroupMember> members) {
        List<Integer> userIds = members.stream()
                .map(GroupMember::getUserId)
                .toList();

        return userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, User::getName));
    }

    private Group findGroupByCode(String groupCode) {
        if (!StringUtils.hasText(groupCode) || groupCode.length() > MAX_GROUP_CODE_LENGTH) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return groupRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private String generateGroupCode() {
        for (int attempt = 0; attempt < GROUP_CODE_GENERATION_ATTEMPTS; attempt++) {
            String groupCode = UUID.randomUUID().toString();
            if (!groupRepository.existsByGroupCode(groupCode)) {
                return groupCode;
            }
        }

        throw new ApiException(ErrorCode.CONFLICT);
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name) || name.length() > MAX_NAME_LENGTH) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
