package com.academicshare.backend.auth.config;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.error.ErrorResponse;
import com.academicshare.backend.common.error.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ErrorResponseFactory errorResponseFactory,
            ObjectMapper objectMapper
    ) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = csrfTokenRepository();

        return http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                        .ignoringRequestMatchers("/auth/signup", "/auth/login")
                )
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, errorResponseFactory, objectMapper, ErrorCode.ACCESS_DENIED))
                        .authenticationEntryPoint((request, response, authenticationException) ->
                                writeError(response, errorResponseFactory, objectMapper, ErrorCode.AUTHENTICATION_REQUIRED))
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .build();
    }

    private CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        return repository;
    }

    private static class CsrfCookieFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            DeferredCsrfToken deferredCsrfToken =
                    (DeferredCsrfToken) request.getAttribute(DeferredCsrfToken.class.getName());
            if (deferredCsrfToken != null) {
                deferredCsrfToken.get().getToken();
                filterChain.doFilter(request, response);
                return;
            }

            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken == null) {
                csrfToken = (CsrfToken) request.getAttribute("_csrf");
            }
            if (csrfToken != null) {
                csrfToken.getToken();
            }

            filterChain.doFilter(request, response);
        }
    }

    private static class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

        @Override
        public void handle(
                HttpServletRequest request,
                HttpServletResponse response,
                Supplier<CsrfToken> deferredCsrfToken
        ) {
            super.handle(request, response, deferredCsrfToken);
            deferredCsrfToken.get().getToken();
        }
    }

    private void writeError(
            HttpServletResponse response,
            ErrorResponseFactory errorResponseFactory,
            ObjectMapper objectMapper,
            ErrorCode errorCode
    ) throws IOException {
        ErrorResponse errorResponse = errorResponseFactory.from(errorCode);

        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
