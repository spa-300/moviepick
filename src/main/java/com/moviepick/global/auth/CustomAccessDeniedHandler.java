package com.moviepick.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

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

        // 접근 권한이 부족하면 403 반환
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // JSON 형식으로 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 오류 응답 생성
        Map<String, Object> errorResponse = Map.of(
                "status", 403,
                "message", "해당 요청에 접근할 권한이 없습니다."
        );

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}
