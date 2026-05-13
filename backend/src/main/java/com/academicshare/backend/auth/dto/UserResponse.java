package com.academicshare.backend.auth.dto;

import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.domain.UserRole;
import com.academicshare.backend.user.domain.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record UserResponse(
        Integer id,
        String loginId,
        String name,
        String emailAddress,
        LocalDateTime createdAt,
        LocalDateTime deletedAt,
        UserStatus status,
        UserRole role
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getEmailAddress(),
                user.getCreatedAt(),
                user.getDeletedAt(),
                user.getStatus(),
                user.getRole()
        );
    }
}
