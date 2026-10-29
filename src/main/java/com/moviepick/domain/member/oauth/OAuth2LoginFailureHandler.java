package com.moviepick.domain.member.oauth;

import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.global.config.FrontendUrls;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * 소셜 로그인 실패 시 프론트 실패 화면(/oauth2/fail?reason=코드)으로 이동한다.
 * reason은 정해진 코드만 붙이고, 예외 문구는 주소에 넣지 않는다.
 */
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    public static final String REASON_MEMBER_SUSPENDED = "MEMBER_SUSPENDED";
    public static final String REASON_MEMBER_WITHDRAWN = "MEMBER_WITHDRAWN";
    public static final String REASON_OAUTH2_FAILED = "OAUTH2_FAILED";

    private static final Set<String> MEMBER_REASONS = Set.of(
            REASON_MEMBER_SUSPENDED,
            REASON_MEMBER_WITHDRAWN
    );

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        response.sendRedirect(FrontendUrls.oauth2Fail(reasonOf(exception)));
    }

    // 정지·탈퇴 회원 로그인 차단 예외 (사용자 서비스에서 사용)
    public static OAuth2AuthenticationException inactiveMemberException(
            MemberStatus status
    ) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(reasonOf(status)),
                "로그인할 수 없는 회원입니다."
        );
    }

    // 회원 상태별 reason (정지·탈퇴 외에는 일반 실패)
    public static String reasonOf(MemberStatus status) {
        if (status == MemberStatus.SUSPENDED) {
            return REASON_MEMBER_SUSPENDED;
        }

        if (status == MemberStatus.WITHDRAWN) {
            return REASON_MEMBER_WITHDRAWN;
        }

        return REASON_OAUTH2_FAILED;
    }

    private static String reasonOf(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String code = oauth2Exception.getError().getErrorCode();

            if (MEMBER_REASONS.contains(code)) {
                return code;
            }
        }

        return REASON_OAUTH2_FAILED;
    }
}
