package com.academicshare.backend.auth.session;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.error.ErrorResponse;
import com.academicshare.backend.common.error.ErrorResponseFactory;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.domain.UserStatus;
import com.academicshare.backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationPathMatcher authenticationPathMatcher;
    private final UserRepository userRepository;
    private final ErrorResponseFactory errorResponseFactory;
    private final ObjectMapper objectMapper;

    public SessionAuthenticationFilter(
            AuthenticationPathMatcher authenticationPathMatcher,
            UserRepository userRepository,
            ErrorResponseFactory errorResponseFactory,
            ObjectMapper objectMapper
    ) {
        this.authenticationPathMatcher = authenticationPathMatcher;
        this.userRepository = userRepository;
        this.errorResponseFactory = errorResponseFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !authenticationPathMatcher.requiresAuthentication(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer currentUserId = readCurrentUserId(session);

        if (currentUserId == null) {
            writeError(response, ErrorCode.AUTHENTICATION_REQUIRED);
            return;
        }

        User user = userRepository.findById(currentUserId).orElse(null);
        if (user == null) {
            invalidate(session);
            writeError(response, ErrorCode.AUTHENTICATION_REQUIRED);
            return;
        }

        if (user.getStatus() == UserStatus.DELETED) {
            invalidate(session);
            writeError(response, ErrorCode.ACCESS_DENIED);
            return;
        }

        request.setAttribute(
                CurrentUserProvider.CURRENT_USER_ATTRIBUTE,
                new AuthenticatedUser(user.getId(), user.getRole())
        );

        filterChain.doFilter(request, response);
    }

    private Integer readCurrentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object currentUserId = session.getAttribute(AuthSessionAttributes.CURRENT_USER_ID);
        return currentUserId instanceof Integer id ? id : null;
    }

    private void invalidate(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        ErrorResponse errorResponse = errorResponseFactory.from(errorCode);

        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
