package com.academicshare.backend.group.dto;

import com.academicshare.backend.group.domain.Group;
import java.time.LocalDateTime;

public record GroupResponse(
        Integer id,
        String groupCode,
        String name,
        Integer leaderId,
        LocalDateTime createdAt
) {

    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getGroupCode(),
                group.getName(),
                group.getLeaderId(),
                group.getCreatedAt()
        );
    }
}
