package com.academicshare.backend.auth.session;

import com.academicshare.backend.user.domain.UserRole;

public record AuthenticatedUser(
        Integer id,
        UserRole role
) {
}
