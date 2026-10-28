package com.moviepick.domain.community.controller;

import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.community.service.CommentLikeService;
import com.moviepick.domain.community.service.CommentService;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.global.auth.CustomAccessDeniedHandler;
import com.moviepick.global.auth.CustomAuthenticationEntryPoint;
import com.moviepick.global.auth.CustomOAuth2UserService;
import com.moviepick.global.auth.CustomOidcUserService;
import com.moviepick.global.auth.JwtProvider;
import com.moviepick.global.auth.OAuth2LoginSuccessHandler;
import com.moviepick.global.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// COMM-04: 댓글 수정은 PATCH /api/community/comments/{commentId}, 기존 PUT 주소는 없음
@WebMvcTest(
        controllers = CommentController.class,
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
        CommentService.class
})
@ActiveProfiles("test")
class CommentUpdatePathTest {

    private static final Long WRITER_ID = 1L;
    private static final Long POST_ID = 3L;
    private static final Long COMMENT_ID = 7L;
    private static final String BODY = "{\"content\": \"수정한 댓글\"}";

    @Autowired
    private MockMvc mockMvc;

    // 실제 서비스를 쓰고 저장소만 모킹한다
    @MockitoBean
    private CommentRepository commentRepository;
    @MockitoBean
    private PostRepository postRepository;
    @MockitoBean
    private MemberRepository memberRepository;
    @MockitoBean
    private CommentLikeService commentLikeService;

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
        Member writer = Member.createLocal("w@test.com", "encoded", "writer");
        ReflectionTestUtils.setField(writer, "id", WRITER_ID);
        Post post = Post.create(writer, "title", "content");
        ReflectionTestUtils.setField(post, "id", POST_ID);
        Comment comment = Comment.create(post, writer, "원래 댓글");
        ReflectionTestUtils.setField(comment, "id", COMMENT_ID);

        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(comment));
        given(postRepository.findByIdAndStatus(POST_ID, PostStatus.VISIBLE)).willReturn(Optional.of(post));
    }

    private MockHttpServletRequestBuilder asWriter(MockHttpServletRequestBuilder request) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                WRITER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return request.with(authentication(auth)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY);
    }

    @Test
    @DisplayName("PATCH /api/community/comments/{commentId} → 200, 수정된 내용")
    void patch_ok() throws Exception {
        mockMvc.perform(asWriter(patch("/api/community/comments/{commentId}", COMMENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(COMMENT_ID))
                .andExpect(jsonPath("$.data.postId").value(POST_ID))
                .andExpect(jsonPath("$.data.content").value("수정한 댓글"));
    }

    @Test
    @DisplayName("기존 PUT /api/community/posts/{postId}/comments/{commentId} → 404 PATH_NOT_FOUND")
    void oldPut_removed() throws Exception {
        mockMvc.perform(asWriter(put("/api/community/posts/{postId}/comments/{commentId}", POST_ID, COMMENT_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PATH_NOT_FOUND"));
    }
}
