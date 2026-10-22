
package com.moviepick.global.auth;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.service.MemberService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    // 토큰 재발급 요청은 Access Token 검증에서 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/api/auth/refresh".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization 헤더 가져오기
        String authorization = request.getHeader("Authorization");

        // 2. Authorization 헤더가 없으면 다음 필터로 이동
        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Bearer 형식이 아니면 401 반환
        if (!authorization.startsWith("Bearer ")) {
            sendUnauthorized(request, response);
            return;
        }

        // 4. JWT 추출
        String token = authorization.substring(7);

        try {
            // 5. JWT 서명 및 만료 시간 검증
            Claims claims = jwtProvider.validateToken(token);

            // 6. Access Token인지 확인
            String type = claims.get("type", String.class);

            if (!"access".equals(type)) {
                throw new IllegalArgumentException(
                        "Access Token이 아닙니다."
                );
            }

            // 7. 회원 ID 추출
            Long memberId = Long.valueOf(claims.getSubject());

            // DB에서 회원 조회 및 ACTIVE 상태 확인
            Member member;

            try {
                member = memberService.getActiveMember(memberId);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(
                        "존재하지 않거나 이용할 수 없는 회원입니다.",
                        e
                );
            }

            // DB에 저장된 현재 권한 사용
            if (member.getRole() == null) {
                throw new IllegalArgumentException(
                        "회원 권한 정보가 없습니다."
                );
            }

            String role = member.getRole().name();

            // 8. 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            memberId,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                            )
                    );

            // 9. SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (IllegalArgumentException e) {

            // 10. JWT 검증 실패 시 인증 정보 제거
            SecurityContextHolder.clearContext();

            // 11. 401 응답 반환
            sendUnauthorized(request, response);
            return;
        }

        // 12. 정상 토큰이면 다음 필터 실행
        filterChain.doFilter(request, response);
    }

    // 인증 실패 시 401 응답 처리
    private void sendUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {

        customAuthenticationEntryPoint.commence(
                request,
                response,
                new BadCredentialsException(
                        "유효하지 않은 인증 정보입니다."
                )
        );
    }
}
