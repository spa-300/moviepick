package com.moviepick.domain.admin.controller;

import com.moviepick.domain.admin.repository.AdminSanctionRepository;
import com.moviepick.domain.admin.service.AdminMemberService;
import com.moviepick.domain.admin.service.AdminReportService;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.domain.community.repository.CommunityReportRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.global.auth.CustomAccessDeniedHandler;
import com.moviepick.global.auth.CustomAuthenticationEntryPoint;
import com.moviepick.global.auth.CustomOAuth2UserService;
import com.moviepick.global.auth.CustomOidcUserService;
import com.moviepick.global.auth.JwtProvider;
import com.moviepick.global.auth.OAuth2LoginSuccessHandler;
import com.moviepick.global.config.SecurityConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ADMIN-05: 관리자 신고·회원 목록이 size를 받고, 기본값·범위는 공통 페이지 규칙(기본 20, 1~100)을 따른다
@WebMvcTest(
        controllers = {AdminReportController.class, AdminMemberController.class},
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
        AdminReportService.class,
        AdminMemberService.class
})
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class AdminListSizeTest {

    @Autowired
    private MockMvc mockMvc;

    // 실제 서비스를 쓰고 저장소만 모킹해, 서비스에 전달된 페이지 크기를 응답 size로 확인한다
    @MockitoBean
    private CommunityReportRepository communityReportRepository;
    @MockitoBean
    private AdminSanctionRepository adminSanctionRepository;
    @MockitoBean
    private MemberRepository memberRepository;
    @MockitoBean
    private PostRepository postRepository;
    @MockitoBean
    private CommentRepository commentRepository;
    @MockitoBean
    private EntityManager entityManager;

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

    @BeforeEach
    void setUp() {
        given(communityReportRepository.findAll(any(Pageable.class)))
                .willAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(0, Pageable.class), 0));
        given(memberRepository.findAll(any(Pageable.class)))
                .willAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(0, Pageable.class), 0));
    }

    @Test
    @DisplayName("신고 목록 size=50 → 50, 생략 → 20")
    void reports_size() throws Exception {
        mockMvc.perform(get("/api/admin/reports").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size").value(50));
        mockMvc.perform(get("/api/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("회원 목록 size=50 → 50, 생략 → 20")
    void members_size() throws Exception {
        mockMvc.perform(get("/api/admin/members").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size").value(50));
        mockMvc.perform(get("/api/admin/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("size=101 → 400 INVALID_PAGE_REQUEST")
    void sizeOutOfRange() throws Exception {
        mockMvc.perform(get("/api/admin/reports").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAGE_REQUEST"));
        mockMvc.perform(get("/api/admin/members").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAGE_REQUEST"));
    }
}
