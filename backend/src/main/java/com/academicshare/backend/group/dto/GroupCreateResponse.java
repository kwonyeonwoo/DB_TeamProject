package com.academicshare.backend.group.dto;

import com.academicshare.backend.group.domain.Group;
import com.academicshare.backend.group.domain.GroupMember;

public record GroupCreateResponse(
        GroupResponse group,
        GroupMemberResponse membership
) {

    public static GroupCreateResponse from(Group group, GroupMember membership) {
        return from(group, membership, null);
    }

    public static GroupCreateResponse from(Group group, GroupMember membership, String userName) {
        return new GroupCreateResponse(
                GroupResponse.from(group),
                GroupMemberResponse.from(membership, userName)
        );
    }
}
