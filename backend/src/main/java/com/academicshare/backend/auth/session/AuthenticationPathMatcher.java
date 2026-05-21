package com.academicshare.backend.auth.session;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class AuthenticationPathMatcher {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<PublicRoute> publicRoutes = List.of(
            new PublicRoute(HttpMethod.POST, "/auth/signup"),
            new PublicRoute(HttpMethod.POST, "/auth/login")
    );

    public boolean requiresAuthentication(HttpServletRequest request) {
        String requestPath = pathWithinApplication(request);
        HttpMethod requestMethod = HttpMethod.valueOf(request.getMethod());

        return publicRoutes.stream()
                .noneMatch(route -> route.matches(requestMethod, requestPath, pathMatcher));
    }

    private String pathWithinApplication(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }

        return requestUri.isBlank() ? "/" : requestUri;
    }

    private record PublicRoute(
            HttpMethod method,
            String pattern
    ) {

        private boolean matches(HttpMethod requestMethod, String requestPath, AntPathMatcher pathMatcher) {
            return method == requestMethod && pathMatcher.match(pattern, requestPath);
        }
    }
}
