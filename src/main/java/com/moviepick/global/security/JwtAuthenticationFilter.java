package com.moviepick.global.security;

import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.ErrorCode;
import com.moviepick.global.response.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final MemberAccessChecker memberAccessChecker;
    private final JwtProvider jwtProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final ObjectMapper objectMapper;

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

        // 공개 주소는 잘못된 토큰을 무시하고 비로그인으로 진행한다
        boolean publicEndpoint = PublicEndpoints.matches(request);

        // 3. Bearer 형식이 아니면 인증 실패
        if (!authorization.startsWith("Bearer ")) {
            reject(request, response, filterChain, publicEndpoint, null);
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
            // 회원 없음·정지·탈퇴(BusinessException)만 인증 실패로 보고,
            // DB 오류 같은 인프라 오류는 그대로 던져 공통 서버 오류(500)로 응답한다
            String role;

            try {
                role = memberAccessChecker.loadActiveRole(memberId);
            } catch (BusinessException e) {
                // 정지·탈퇴 회원은 보호 주소에서 로그인·refresh와 같은 403으로 응답
                reject(request, response, filterChain, publicEndpoint, forbiddenCode(e));
                return;
            }

            // DB에 저장된 현재 권한 사용
            if (role == null) {
                throw new IllegalArgumentException(
                        "회원 권한 정보가 없습니다."
                );
            }

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

            // 10. JWT 검증 실패 (형식·서명·만료·토큰 종류)
            reject(request, response, filterChain, publicEndpoint, null);
            return;
        }

        // 11. 정상 토큰이면 다음 필터 실행
        filterChain.doFilter(request, response);
    }

    // 인증 실패 처리
    // 공개 주소: 인증 정보 없이 다음 필터로 진행 (비로그인과 같은 응답)
    // 보호 주소: forbiddenCode가 있으면 403, 없으면 401
    private void reject(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            boolean publicEndpoint,
            ErrorCode forbiddenCode
    ) throws IOException, ServletException {

        SecurityContextHolder.clearContext();

        if (publicEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        if (forbiddenCode != null) {
            sendError(response, forbiddenCode);
            return;
        }

        sendUnauthorized(request, response);
    }

    // 정지·탈퇴 회원이면 그 오류 이름, 그 밖(회원 없음 등)은 null → 401
    private ErrorCode forbiddenCode(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        if (memberAccessChecker.isForbidden(errorCode)) {
            return errorCode;
        }

        return null;
    }

    // 공통 응답 모양으로 오류 응답
    private void sendError(
            HttpServletResponse response,
            ErrorCode errorCode
    ) throws IOException {

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.error(errorCode)
        );
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
