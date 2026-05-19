package com.academicshare.backend.group.dto;

import com.academicshare.backend.group.domain.GroupMember;
import com.academicshare.backend.group.domain.GroupMemberRole;
import java.time.LocalDateTime;

public record GroupMemberResponse(
        Integer groupId,
        Integer userId,
        String userName,
        GroupMemberRole role,
        LocalDateTime joinedAt
) {

    public static GroupMemberResponse from(GroupMember groupMember) {
        return from(groupMember, null);
    }

    public static GroupMemberResponse from(GroupMember groupMember, String userName) {
        return new GroupMemberResponse(
                groupMember.getGroupId(),
                groupMember.getUserId(),
                userName,
                groupMember.getRole(),
                groupMember.getJoinedAt()
        );
    }
}
