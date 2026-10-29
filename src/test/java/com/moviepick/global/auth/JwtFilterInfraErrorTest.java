package com.moviepick.global.auth;

import com.moviepick.domain.member.service.MemberStatusChecker;
import com.moviepick.domain.member.controller.MemberController;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.global.config.FilterErrorConfig;
import com.moviepick.global.config.SecurityConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// AUTH-04: 인증 필터 안의 DB 같은 인프라 오류는 401이 아니라 서버 오류로 응답한다
@WebMvcTest(
        controllers = MemberController.class,
        properties = {
                "spring.security.oauth2.client.registration.google.client-id=test-google",
                "spring.security.oauth2.client.registration.google.client-secret=test-secret",
                "spring.security.oauth2.client.registration.kakao.client-id=test-kakao",
                "spring.security.oauth2.client.registration.kakao.client-secret=test-secret"
        }
)
@Import({
        SecurityConfig.class,
        FilterErrorConfig.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        MemberStatusChecker.class,
        OAuth2LoginFailureHandler.class
})
@ActiveProfiles("test")
class JwtFilterInfraErrorTest {

    private static final String TOKEN = "valid-access-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    @MockitoBean
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Test
    @DisplayName("필터에서 회원 조회 중 DB 오류가 나면 500 INTERNAL_ERROR")
    void dbError_internalError() throws Exception {
        Claims claims = Jwts.claims().subject("1").add("type", "access").build();
        given(jwtProvider.validateToken(TOKEN)).willReturn(claims);
        given(memberService.getActiveMember(1L))
                .willThrow(new DataAccessResourceFailureException("Communications link failure"));

        mockMvc.perform(get("/api/members/me")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"));
    }
}
