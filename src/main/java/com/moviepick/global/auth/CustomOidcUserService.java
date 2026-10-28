package com.moviepick.global.auth;

import com.moviepick.domain.member.entity.AuthProvider;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        String registrationId = userRequest
                .getClientRegistration()
                .getRegistrationId();

        if (!"google".equals(registrationId)) {
            throw new OAuth2AuthenticationException(
                    "지원하지 않는 소셜 로그인입니다."
            );
        }

        // 구글 계정의 고유 식별자
        String providerId = oidcUser.getSubject();

        if (providerId == null || providerId.isBlank()) {
            throw new OAuth2AuthenticationException(
                    "구글 사용자 ID를 찾을 수 없습니다."
            );
        }

        String email = oidcUser.getEmail();
        String nickname = oidcUser.getFullName();

        if (nickname == null || nickname.isBlank()) {
            nickname = "구글회원_" + providerId;
        }

        final String finalNickname = nickname;

        // 기존 회원 조회, 없으면 신규 회원 생성
        Member member = memberRepository
                .findByProviderAndProviderId(
                        AuthProvider.GOOGLE,
                        providerId
                )
                .orElseGet(() -> memberRepository.save(
                        Member.createSocial(
                                AuthProvider.GOOGLE,
                                providerId,
                                email,
                                finalNickname
                        )
                ));


        // 정지 기간이 만료되었다면 자동 해제
        member.releaseSuspensionIfExpired();

        // 아직 정지 중이거나 탈퇴한 회원은 로그인 차단
        // 실패 처리기가 정지·탈퇴를 구분할 수 있도록 오류 코드를 붙인다
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw OAuth2LoginFailureHandler.inactiveMemberException(
                    member.getStatus()
            );
        }


        return oidcUser;
    }
}