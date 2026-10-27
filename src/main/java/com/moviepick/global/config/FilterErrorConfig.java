package com.moviepick.global.config;

import com.moviepick.global.exception.CommonErrorCode;
import com.moviepick.global.response.ApiResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 필터 단계(JWT 필터 등)에서 잡히지 않은 예외를 공통 응답 모양으로 바꾼다.
 * 컨트롤러 밖이라 GlobalExceptionHandler가 잡지 못하는 오류용.
 */
@Configuration
@RequiredArgsConstructor
public class FilterErrorConfig {

    // Spring Security 필터 순서(-100)보다 먼저 실행되어야 그 안의 예외를 잡을 수 있다
    static final int ORDER = -101;

    private final ObjectMapper objectMapper;

    @Bean
    public FilterRegistrationBean<FilterErrorFilter> filterErrorFilterRegistration() {
        FilterRegistrationBean<FilterErrorFilter> registration =
                new FilterRegistrationBean<>(new FilterErrorFilter(objectMapper));
        registration.setOrder(ORDER);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Slf4j
    @RequiredArgsConstructor
    static class FilterErrorFilter extends OncePerRequestFilter {

        private final ObjectMapper objectMapper;

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                // 이미 응답이 나가기 시작했다면 바꿀 수 없음
                if (response.isCommitted()) {
                    throw e;
                }

                CommonErrorCode errorCode;
                if (isAuthenticationError(e)) {
                    log.warn("Filter authentication error: {}", e.getClass().getSimpleName());
                    errorCode = CommonErrorCode.UNAUTHORIZED;
                } else {
                    log.error("Unexpected filter error", e);
                    errorCode = CommonErrorCode.INTERNAL_ERROR;
                }

                // reset() 대신 resetBuffer(): 이미 붙은 CORS 헤더는 유지하고 본문만 비운다
                response.resetBuffer();
                response.setStatus(errorCode.getStatus().value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(response.getWriter(), ApiResponse.error(errorCode));
            }
        }

        private boolean isAuthenticationError(Throwable e) {
            for (Throwable cause = e; cause != null; cause = cause.getCause()) {
                if (cause instanceof JwtException || cause instanceof AuthenticationException) {
                    return true;
                }
            }
            return false;
        }
    }
}
