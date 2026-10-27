package com.moviepick.global.config;

import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FilterErrorConfigTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final FilterErrorConfig.FilterErrorFilter filter =
            new FilterErrorConfig.FilterErrorFilter(objectMapper);

    private JsonNode run(RuntimeException toThrow, MockHttpServletResponse response) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/members/me");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");

        filter.doFilter(request, response, (req, res) -> {
            throw toThrow;
        });

        return objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("필터 순서는 Spring Security 필터(-100)보다 앞")
    void order_beforeSecurity() {
        assertThat(FilterErrorConfig.ORDER).isLessThan(-100);
    }

    @Test
    @DisplayName("필터에서 난 JWT 예외는 401 UNAUTHORIZED, CORS 헤더 유지")
    void jwtException_401() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        JsonNode body = run(new MalformedJwtException("bad token"), response);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("errorCode").asString()).isEqualTo("UNAUTHORIZED");
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("http://localhost:5173");
    }

    @Test
    @DisplayName("필터에서 난 그 외 예외는 500 INTERNAL_ERROR, 내부 메시지 숨김")
    void otherException_500() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        JsonNode body = run(new IllegalStateException("db is down"), response);

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(body.get("errorCode").asString()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.get("message").asString()).doesNotContain("db is down");
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("http://localhost:5173");
    }
}
