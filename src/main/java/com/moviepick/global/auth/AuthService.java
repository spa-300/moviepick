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

        // 5. Refresh Token 생성
        String refreshToken = jwtProvider.createRefreshToken(
                member.getId()
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
}