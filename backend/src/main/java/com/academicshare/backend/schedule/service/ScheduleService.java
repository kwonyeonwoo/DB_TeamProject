package com.academicshare.backend.schedule.service;

import com.academicshare.backend.auth.session.CurrentUserProvider;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.group.repository.GroupMemberRepository;
import com.academicshare.backend.group.repository.GroupRepository;
import com.academicshare.backend.schedule.domain.Schedule;
import com.academicshare.backend.schedule.dto.ScheduleCreateRequest;
import com.academicshare.backend.schedule.dto.ScheduleResponse;
import com.academicshare.backend.schedule.dto.ScheduleUpdateRequest;
import com.academicshare.backend.schedule.repository.ScheduleRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ScheduleService {

    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MIN_TYPE = 1;
    private static final int MAX_TYPE = 5;

    private final ScheduleRepository scheduleRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CurrentUserProvider currentUserProvider;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.scheduleRepository = scheduleRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedules(LocalDateTime startAt, LocalDateTime endAt) {
        validatePeriod(startAt, endAt);

        return scheduleRepository.findPersonalSchedules(currentUserProvider.getCurrentUserId(), startAt, endAt)
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional
    public ScheduleResponse createMySchedule(ScheduleCreateRequest request) {
        validateCreateRequest(request);

        Schedule schedule = scheduleRepository.saveAndFlush(new Schedule(
                currentUserProvider.getCurrentUserId(),
                null,
                request.title(),
                request.startAt(),
                request.endAt(),
                request.description(),
                request.type()
        ));

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public ScheduleResponse updateMySchedule(Integer scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = findSchedule(scheduleId);
        requirePersonalOwner(schedule);
        validateUpdateRequest(schedule, request);

        if (request.titleProvided()) {
            schedule.changeTitle(request.title());
        }
        if (request.startAtProvided()) {
            schedule.changeStartAt(request.startAt());
        }
        if (request.endAtProvided()) {
            schedule.changeEndAt(request.endAt());
        }
        if (request.descriptionProvided()) {
            schedule.changeDescription(request.description());
        }
        if (request.typeProvided()) {
            schedule.changeType(request.type());
        }
        schedule.markUpdated();

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public void deleteMySchedule(Integer scheduleId) {
        Schedule schedule = findSchedule(scheduleId);
        requirePersonalOwner(schedule);
        scheduleRepository.delete(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getGroupSchedules(Integer groupId, LocalDateTime startAt, LocalDateTime endAt) {
        validatePeriod(startAt, endAt);
        requireGroupMember(groupId);

        return scheduleRepository.findGroupSchedules(groupId, startAt, endAt)
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional
    public ScheduleResponse createGroupSchedule(Integer groupId, ScheduleCreateRequest request) {
        requireGroupMember(groupId);
        validateCreateRequest(request);

        Schedule schedule = scheduleRepository.saveAndFlush(new Schedule(
                currentUserProvider.getCurrentUserId(),
                groupId,
                request.title(),
                request.startAt(),
                request.endAt(),
                request.description(),
                request.type()
        ));

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public ScheduleResponse updateGroupSchedule(Integer groupId, Integer scheduleId, ScheduleUpdateRequest request) {
        requireGroupMember(groupId);
        Schedule schedule = findSchedule(scheduleId);
        requireGroupSchedule(schedule, groupId);
        validateUpdateRequest(schedule, request);

        if (request.titleProvided()) {
            schedule.changeTitle(request.title());
        }
        if (request.startAtProvided()) {
            schedule.changeStartAt(request.startAt());
        }
        if (request.endAtProvided()) {
            schedule.changeEndAt(request.endAt());
        }
        if (request.descriptionProvided()) {
            schedule.changeDescription(request.description());
        }
        if (request.typeProvided()) {
            schedule.changeType(request.type());
        }
        schedule.markUpdated();

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public void deleteGroupSchedule(Integer groupId, Integer scheduleId) {
        requireGroupMember(groupId);
        Schedule schedule = findSchedule(scheduleId);
        requireGroupSchedule(schedule, groupId);
        scheduleRepository.delete(schedule);
    }

    private Schedule findSchedule(Integer scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private void requirePersonalOwner(Schedule schedule) {
        if (!schedule.isPersonal() || !schedule.isOwnedBy(currentUserProvider.getCurrentUserId())) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireGroupMember(Integer groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUserProvider.getCurrentUserId())) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireGroupSchedule(Schedule schedule, Integer groupId) {
        if (!groupId.equals(schedule.getGroupId())) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    private void validateCreateRequest(ScheduleCreateRequest request) {
        validateRequiredText(request.title(), MAX_TITLE_LENGTH);
        validateRequiredDateTime(request.startAt());
        validateRequiredDateTime(request.endAt());
        validateType(request.type());
        validateDescription(request.description());
        validatePeriod(request.startAt(), request.endAt());
    }

    private void validateUpdateRequest(Schedule schedule, ScheduleUpdateRequest request) {
        if (!request.hasAnyField()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.titleProvided()) {
            validateRequiredText(request.title(), MAX_TITLE_LENGTH);
        }
        if (request.startAtProvided()) {
            validateRequiredDateTime(request.startAt());
        }
        if (request.endAtProvided()) {
            validateRequiredDateTime(request.endAt());
        }
        if (request.descriptionProvided()) {
            validateDescription(request.description());
        }
        if (request.typeProvided()) {
            validateType(request.type());
        }

        LocalDateTime startAt = request.startAtProvided() ? request.startAt() : schedule.getStartAt();
        LocalDateTime endAt = request.endAtProvided() ? request.endAt() : schedule.getEndAt();
        validatePeriod(startAt, endAt);
    }

    private void validateRequiredText(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() > maxLength) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateRequiredDateTime(LocalDateTime value) {
        if (value == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateType(Integer type) {
        if (type == null || type < MIN_TYPE || type > MAX_TYPE) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validatePeriod(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
