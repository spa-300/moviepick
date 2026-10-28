package com.moviepick.global.auth;

/**
 * 소셜 로그인 후 이동할 프론트 주소.
 * 성공·실패 처리기가 같은 주소를 쓰도록 한 곳에 둔다.
 */
public final class FrontendUrls {

    public static final String BASE = "http://localhost:5173";

    public static final String OAUTH2_SUCCESS = BASE + "/oauth2/success";

    private FrontendUrls() {
    }

    // reason은 OAuth2LoginFailureHandler가 정한 고정 코드만 받는다
    public static String oauth2Fail(String reason) {
        return BASE + "/oauth2/fail?reason=" + reason;
    }
}
