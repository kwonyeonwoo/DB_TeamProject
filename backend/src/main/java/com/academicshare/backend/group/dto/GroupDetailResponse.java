package com.academicshare.backend.group.dto;

import com.academicshare.backend.group.domain.Group;
import com.academicshare.backend.group.domain.GroupMember;
import java.util.List;
import java.util.Map;

public record GroupDetailResponse(
        GroupResponse group,
        List<GroupMemberResponse> members
) {

    public GroupDetailResponse {
        members = members == null ? List.of() : List.copyOf(members);
    }

    public static GroupDetailResponse from(Group group, List<GroupMember> members, Map<Integer, String> userNamesById) {
        return new GroupDetailResponse(
                GroupResponse.from(group),
                members.stream()
                        .map(member -> GroupMemberResponse.from(member, userNamesById.get(member.getUserId())))
                        .toList()
        );
    }
}
