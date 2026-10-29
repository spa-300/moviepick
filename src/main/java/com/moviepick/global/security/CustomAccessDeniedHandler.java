package com.moviepick.global.security;

import com.moviepick.global.exception.CommonErrorCode;
import com.moviepick.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        // CSRF 토큰이 없거나 틀린 경우는 오류 이름으로 다른 403과 구분
        CommonErrorCode errorCode = accessDeniedException instanceof CsrfException
                ? CommonErrorCode.CSRF_INVALID
                : CommonErrorCode.FORBIDDEN;

        // 접근 권한이 부족하면 403 반환
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 공통 응답 모양(JSON)으로 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.error(errorCode)
        );
    }
}
