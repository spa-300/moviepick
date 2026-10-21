package com.moviepick.global.auth;

import com.moviepick.domain.member.dto.request.LoginRequest;
import com.moviepick.domain.member.dto.request.SignupRequest;
import com.moviepick.domain.member.dto.response.LoginResponse;
import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.exception.MemberErrorCode;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    // 회원가입
    @Transactional
    public MemberResponse signup(SignupRequest request) {

        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(
                    MemberErrorCode.DUPLICATE_EMAIL
            );
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        Member member = Member.createLocal(
                request.email(),
                encodedPassword,
                request.nickname()
        );

        Member savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    // 로그인
    public LoginResult login(LoginRequest request) {

        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(
                        MemberErrorCode.INVALID_CREDENTIALS
                ));

        // 2. 비밀번호 검증
        if (member.getPasswordHash() == null ||
                !passwordEncoder.matches(
                        request.password(),
                        member.getPasswordHash()
                )) {
            throw new BusinessException(
                    MemberErrorCode.INVALID_CREDENTIALS
            );
        }

        // 3. 회원 상태 확인
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(
                    MemberErrorCode.MEMBER_SUSPENDED
            );
        }

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(
                    MemberErrorCode.MEMBER_WITHDRAWN
            );
        }

        // 4. Access Token 생성
        String accessToken = jwtProvider.createAccessToken(
                member.getId(),
                member.getRole().name()
        );


        // 5. 회원의 현재 인증 버전 조회
        long authVersion =
                refreshTokenService.getAuthVersion(member.getId());

        // 6. 인증 버전이 포함된 Refresh Token 생성
        String refreshToken = jwtProvider.createRefreshToken(
                member.getId(),
                authVersion
        );


        // 6. Redis에 Refresh Token 저장
        refreshTokenService.save(
                member.getId(),
                refreshToken
        );

        // 7. 로그인 응답 생성
        LoginResponse loginResponse =
                LoginResponse.of(member, accessToken);

        // 8. Access Token 응답과 Refresh Token 함께 반환
        return new LoginResult(loginResponse, refreshToken);


    }

    // 로그아웃
    public void logout(String refreshToken) {

        // Refresh Token이 없으면 삭제할 토큰도 없으므로 종료
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        // Redis에 저장된 Refresh Token 삭제
        refreshTokenService.delete(refreshToken);
    }


    // Access Token 재발급
    public String refreshAccessToken(String refreshToken) {

        // 1. Refresh Token이 없는 경우
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh Token이 없습니다."
            );
        }

        // 2. JWT 서명, 만료시간, 토큰 종류 검증
        Long memberId = jwtProvider.validateRefreshToken(refreshToken);

        // 3. Redis에 저장된 Refresh Token인지 확인
        if (!refreshTokenService.isValid(memberId, refreshToken)) {
            throw new IllegalArgumentException(
                    "유효하지 않거나 로그아웃된 Refresh Token입니다."
            );
        }


        // 4. Refresh Token의 인증 버전 확인
        Claims claims = jwtProvider.validateToken(refreshToken);

        Number tokenVersion = claims.get("authVersion", Number.class);

        if (tokenVersion == null) {
            throw new IllegalArgumentException(
                    "인증 버전 정보가 없는 Refresh Token입니다."
            );
        }

        // 5. Redis에 저장된 현재 인증 버전 조회
        long currentVersion =
                refreshTokenService.getAuthVersion(memberId);

        // 6. 인증 버전이 다르면 재발급 거부
        if (tokenVersion.longValue() != currentVersion) {
            throw new IllegalArgumentException(
                    "무효화된 Refresh Token입니다. 다시 로그인해주세요."
            );
        }


        // 4. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(
                        MemberErrorCode.MEMBER_NOT_FOUND
                ));

        // 5. 회원 상태 확인
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(
                    MemberErrorCode.MEMBER_SUSPENDED
            );
        }

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(
                    MemberErrorCode.MEMBER_WITHDRAWN
            );
        }

        // 6. 새로운 Access Token 발급
        return jwtProvider.createAccessToken(
                member.getId(),
                member.getRole().name()
        );
    }


}