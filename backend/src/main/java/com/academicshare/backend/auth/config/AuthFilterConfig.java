package com.academicshare.backend.auth.config;

import com.academicshare.backend.auth.session.AuthenticationPathMatcher;
import com.academicshare.backend.auth.session.SessionAuthenticationFilter;
import com.academicshare.backend.common.error.ErrorResponseFactory;
import com.academicshare.backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class AuthFilterConfig {

    @Bean
    public FilterRegistrationBean<SessionAuthenticationFilter> sessionAuthenticationFilter(
            AuthenticationPathMatcher authenticationPathMatcher,
            UserRepository userRepository,
            ErrorResponseFactory errorResponseFactory,
            ObjectMapper objectMapper
    ) {
        SessionAuthenticationFilter filter = new SessionAuthenticationFilter(
                authenticationPathMatcher,
                userRepository,
                errorResponseFactory,
                objectMapper
        );

        FilterRegistrationBean<SessionAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
