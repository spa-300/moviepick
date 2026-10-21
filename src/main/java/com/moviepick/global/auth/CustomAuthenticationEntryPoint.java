package com.moviepick.global.auth;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        // 인증되지 않은 요청은 401 반환
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // JSON 응답 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 클라이언트에 전달할 오류 내용
        Map<String, Object> errorResponse = Map.of(
                "status", 401,
                "message", "로그인이 필요하거나 인증 정보가 유효하지 않습니다."
        );

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}
