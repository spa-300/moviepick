package com.moviepick.global.auth;

import com.moviepick.domain.member.entity.AuthProvider;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {


        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest
                .getClientRegistration()
                .getRegistrationId();

        if (!"kakao".equals(registrationId)) {
            throw new OAuth2AuthenticationException(
                    "지원하지 않는 소셜 로그인입니다."
            );
        }

        Map<String, Object> attributes = oauth2User.getAttributes();

        Object id = attributes.get("id");

        if (id == null) {
            throw new OAuth2AuthenticationException(
                    "카카오 사용자 ID를 찾을 수 없습니다."
            );
        }

        String providerId = String.valueOf(id);

        Map<String, Object> kakaoAccount =
                getMap(attributes.get("kakao_account"));

        String email = getString(kakaoAccount.get("email"));

        Map<String, Object> profile =
                getMap(kakaoAccount.get("profile"));

        String nickname = getString(profile.get("nickname"));

        if (nickname == null || nickname.isBlank()) {
            nickname = "카카오회원_" + providerId;
        }

        final String finalNickname = nickname;

        Member member = memberRepository
                .findByProviderAndProviderId(
                        AuthProvider.KAKAO,
                        providerId
                )
                .orElseGet(() -> memberRepository.save(
                        Member.createSocial(
                                AuthProvider.KAKAO,
                                providerId,
                                email,
                                finalNickname
                        )
                ));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new OAuth2AuthenticationException(
                    "로그인할 수 없는 회원입니다."
            );
        }

        return oauth2User;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private String getString(Object value) {
        return value instanceof String str ? str : null;
    }
}
