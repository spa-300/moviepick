package com.moviepick.domain.movie.controller;

import com.moviepick.domain.member.service.MemberStatusChecker;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.domain.movie.service.MovieCollectionService;
import com.moviepick.global.auth.CustomAccessDeniedHandler;
import com.moviepick.global.auth.OAuth2LoginFailureHandler;
import com.moviepick.global.auth.CustomAuthenticationEntryPoint;
import com.moviepick.global.auth.CustomOAuth2UserService;
import com.moviepick.global.auth.CustomOidcUserService;
import com.moviepick.global.auth.JwtProvider;
import com.moviepick.global.auth.OAuth2LoginSuccessHandler;
import com.moviepick.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminMovieController.class,
        properties = {
                "spring.security.oauth2.client.registration.google.client-id=test-google",
                "spring.security.oauth2.client.registration.google.client-secret=test-secret",
                "spring.security.oauth2.client.registration.kakao.client-id=test-kakao",
                "spring.security.oauth2.client.registration.kakao.client-secret=test-secret"
        }
)
@Import({
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        MemberStatusChecker.class,
        OAuth2LoginFailureHandler.class
})
@ActiveProfiles("test")
class AdminMovieSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieCollectionService movieCollectionService;

    // JWT 필터 생성에 필요한 의존성
    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtProvider jwtProvider;

    // SecurityConfig의 OAuth2 관련 의존성
    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    @MockitoBean
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Test
    @DisplayName("일반 사용자는 영화 동기화 API에 접근할 수 없다")
    @WithMockUser(roles = "USER")
    void syncMovies_user_forbidden() throws Exception {

        mockMvc.perform(post("/api/admin/movies/sync")
                        .param("pages", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(movieCollectionService);
    }

    @Test
    @DisplayName("관리자는 영화 동기화 API를 호출할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void syncMovies_admin_success() throws Exception {

        MovieSyncResponse response =
                new MovieSyncResponse(1, 2, 1);

        given(movieCollectionService
                .collectPopularMoviesWithResult(1))
                .willReturn(response);

        mockMvc.perform(post("/api/admin/movies/sync")
                        .param("pages", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.savedCount").value(2))
                .andExpect(jsonPath("$.data.failedCount").value(1));

        verify(movieCollectionService)
                .collectPopularMoviesWithResult(1);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자는 동기화 API에 접근할 수 없다")
    void syncMovies_anonymous_unauthorized() throws Exception {

        mockMvc.perform(post("/api/admin/movies/sync")
                        .param("pages", "1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(movieCollectionService);
    }

    @Test
    @DisplayName("수집 페이지가 0이면 400 Bad Request를 반환한다")
    @WithMockUser(roles = "ADMIN")
    void syncMovies_invalidPages() throws Exception {

        // when & then
        mockMvc.perform(post("/api/admin/movies/sync")
                        .param("pages", "0")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // 잘못된 요청은 실제 영화 수집을 실행하면 안 된다.
        verifyNoInteractions(movieCollectionService);
    }
}