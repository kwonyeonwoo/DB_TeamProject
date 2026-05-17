package com.academicshare.backend.user.service;

import com.academicshare.backend.auth.session.CurrentUserProvider;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.group.domain.Group;
import com.academicshare.backend.group.domain.GroupMember;
import com.academicshare.backend.group.domain.GroupMemberRole;
import com.academicshare.backend.group.repository.GroupMemberRepository;
import com.academicshare.backend.group.repository.GroupRepository;
import com.academicshare.backend.schedule.repository.ScheduleRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.domain.UserStatus;
import com.academicshare.backend.user.dto.UserUpdateRequest;
import com.academicshare.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_PASSWORD_LENGTH = 255;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            ScheduleRepository scheduleRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            CurrentUserProvider currentUserProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.currentUserProvider = currentUserProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getMe() {
        return getCurrentUser();
    }

    @Transactional
    public User updateMe(UserUpdateRequest request) {
        User user = getCurrentUser();
        validateUpdateRequest(user, request);

        if (request.nameProvided()) {
            user.changeName(request.name());
        }
        if (request.emailAddressProvided()) {
            user.changeEmailAddress(request.emailAddress());
        }
        if (request.newPasswordProvided()) {
            user.changePassword(passwordEncoder.encode(request.newPassword()));
        }

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT);
        }
    }

    @Transactional
    public void deleteMe() {
        User user = getCurrentUser();
        Integer userId = user.getId();

        scheduleRepository.deleteByUserIdAndGroupIdIsNull(userId);
        leaveAllGroups(userId);
        user.markDeleted(LocalDateTime.now());
    }

    @Transactional
    public int clearExpiredDeletedUserPersonalData(LocalDateTime now) {
        LocalDateTime cutoff = now.minusMonths(6);
        List<User> expiredUsers = userRepository.findByStatusAndDeletedAtLessThanEqual(UserStatus.DELETED, cutoff);
        expiredUsers.forEach(User::clearPersonalData);
        return expiredUsers.size();
    }

    private User getCurrentUser() {
        return userRepository.findById(currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.AUTHENTICATION_REQUIRED));
    }

    private void leaveAllGroups(Integer userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserIdOrderByJoinedAtAscGroupIdAsc(userId);

        for (GroupMember membership : memberships) {
            Group group = groupRepository.findById(membership.getGroupId()).orElse(null);
            if (group == null) {
                continue;
            }

            List<GroupMember> members = groupMemberRepository.findByGroupIdOrderByJoinedAtAscUserIdAsc(group.getId());
            List<GroupMember> remainingMembers = members.stream()
                    .filter(member -> !Objects.equals(member.getUserId(), userId))
                    .toList();

            if (remainingMembers.isEmpty()) {
                groupRepository.delete(group);
                continue;
            }

            if (Objects.equals(group.getLeaderId(), userId) || membership.getRole() == GroupMemberRole.LEADER) {
                GroupMember nextLeader = remainingMembers.get(0);
                group.changeLeaderId(nextLeader.getUserId());
                nextLeader.changeRole(GroupMemberRole.LEADER);
            }

            groupMemberRepository.delete(membership);
        }
    }

    private void validateUpdateRequest(User user, UserUpdateRequest request) {
        if (!request.hasAnyUpdateField()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        if (request.nameProvided()) {
            validateName(user, request.name());
        }
        if (request.emailAddressProvided()) {
            validateEmailAddress(user, request.emailAddress());
        }
        if (request.newPasswordProvided()) {
            validatePasswordChange(user, request);
        }
    }

    private void validateName(User user, String name) {
        if (!StringUtils.hasText(name) || name.length() > MAX_NAME_LENGTH) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (Objects.equals(user.getName(), name)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateEmailAddress(User user, String emailAddress) {
        if (!StringUtils.hasText(emailAddress)
                || emailAddress.length() > MAX_EMAIL_LENGTH
                || !EMAIL_PATTERN.matcher(emailAddress).matches()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (Objects.equals(user.getEmailAddress(), emailAddress)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (userRepository.existsByEmailAddressAndIdNot(emailAddress, user.getId())) {
            throw new ApiException(ErrorCode.CONFLICT);
        }
    }

    private void validatePasswordChange(User user, UserUpdateRequest request) {
        if (!StringUtils.hasText(request.newPassword()) || request.newPassword().length() > MAX_PASSWORD_LENGTH) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (!StringUtils.hasText(request.currentPassword())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
