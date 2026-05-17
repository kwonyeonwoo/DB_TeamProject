package com.academicshare.backend.group.dto;

import com.academicshare.backend.group.domain.Group;
import com.academicshare.backend.group.domain.GroupMember;
import java.util.List;

public record GroupDetailResponse(
        GroupResponse group,
        List<GroupMemberResponse> members
) {

    public GroupDetailResponse {
        members = members == null ? List.of() : List.copyOf(members);
    }

    public static GroupDetailResponse from(Group group, List<GroupMember> members) {
        return new GroupDetailResponse(
                GroupResponse.from(group),
                members.stream()
                        .map(GroupMemberResponse::from)
                        .toList()
        );
    }
}
