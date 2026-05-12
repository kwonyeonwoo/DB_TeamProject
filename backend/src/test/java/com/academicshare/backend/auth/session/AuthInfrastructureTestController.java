package com.academicshare.backend.auth.session;

import com.academicshare.backend.common.response.ItemsResponse;
import com.academicshare.backend.user.domain.UserRole;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AuthInfrastructureTestController {

    private final CurrentUserProvider currentUserProvider;

    AuthInfrastructureTestController(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/users/me")
    Map<String, Object> currentUser() {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();
        return Map.of(
                "id", currentUser.id(),
                "role", currentUser.role().name()
        );
    }

    @GetMapping("/admin/reports")
    ItemsResponse<String> adminOnly() {
        currentUserProvider.requireRole(UserRole.ADMIN);
        return new ItemsResponse<>(List.of("report"));
    }
}
