
package com.moviepick.global.auth;

import com.moviepick.domain.member.entity.AuthProvider;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        Object kakaoId = oauth2User.getAttributes().get("id");

        if (kakaoId == null) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "카카오 사용자 ID가 없습니다."
            );
            return;
        }

        String providerId = String.valueOf(kakaoId);

        Member member = memberRepository
                .findByProviderAndProviderId(
                        AuthProvider.KAKAO,
                        providerId
                )
                .orElse(null);

        if (member == null || member.getStatus() != MemberStatus.ACTIVE) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "로그인할 수 없는 회원입니다."
            );
            return;
        }

        // 회원의 현재 인증 버전 조회
        long authVersion = refreshTokenService.getAuthVersion(
                member.getId()
        );

        // Refresh Token 생성
        String refreshToken = jwtProvider.createRefreshToken(
                member.getId(),
                authVersion
        );

        refreshTokenService.save(
                member.getId(),
                refreshToken
        );

        // Refresh Token을 HttpOnly 쿠키로 발급
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // 로컬 HTTP 테스트용
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(14 * 24 * 60 * 60)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        // OAuth2 인증 과정에서 사용한 세션 정리
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        // React 로그인 완료 페이지로 이동
        response.sendRedirect(
                "http://localhost:5173/oauth2/success"
        );
    }
}
