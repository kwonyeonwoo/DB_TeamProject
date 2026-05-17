package com.academicshare.backend.group.dto;

import com.academicshare.backend.group.domain.GroupMember;
import com.academicshare.backend.group.domain.GroupMemberRole;
import java.time.LocalDateTime;

public record GroupMemberResponse(
        Integer groupId,
        Integer userId,
        GroupMemberRole role,
        LocalDateTime joinedAt
) {

    public static GroupMemberResponse from(GroupMember groupMember) {
        return new GroupMemberResponse(
                groupMember.getGroupId(),
                groupMember.getUserId(),
                groupMember.getRole(),
                groupMember.getJoinedAt()
        );
    }
}
