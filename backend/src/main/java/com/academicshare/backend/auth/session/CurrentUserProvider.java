package com.academicshare.backend.auth.session;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.user.domain.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentUserProvider {

    public static final String CURRENT_USER_ATTRIBUTE = CurrentUserProvider.class.getName() + ".CURRENT_USER";

    public AuthenticatedUser getCurrentUser() {
        AuthenticatedUser currentUser = getCurrentUserOrNull();
        if (currentUser == null) {
            throw new ApiException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        return currentUser;
    }

    public Integer getCurrentUserId() {
        return getCurrentUser().id();
    }

    public boolean hasCurrentUser() {
        return getCurrentUserOrNull() != null;
    }

    public void requireRole(UserRole role) {
        AuthenticatedUser currentUser = getCurrentUser();
        if (currentUser.role() != role) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
    }

    private AuthenticatedUser getCurrentUserOrNull() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }

        Object attribute = request.getAttribute(CURRENT_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser currentUser ? currentUser : null;
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }

        return null;
    }
}
