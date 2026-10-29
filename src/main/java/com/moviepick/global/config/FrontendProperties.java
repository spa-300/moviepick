package com.moviepick.global.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml의 app.frontend.* 설정.
 * 소셜 로그인 후 이동할 프론트 주소와 CORS 허용 주소를 한 곳에서 관리한다.
 * 값이 비어 있으면 로컬 React 개발 서버 주소를 쓴다.
 */
@ConfigurationProperties(prefix = "app.frontend")
public record FrontendProperties(
        String url,
        List<String> allowedOrigins
) {

    public static final String DEFAULT_URL = "http://localhost:5173";

    public FrontendProperties {
        if (url == null || url.isBlank()) {
            url = DEFAULT_URL;
        }

        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            allowedOrigins = List.of(url);
        } else {
            allowedOrigins = List.copyOf(allowedOrigins);
        }
    }

    public String oauth2SuccessUrl() {
        return url + "/oauth2/success";
    }

    // reason은 OAuth2LoginFailureHandler가 정한 고정 코드만 받는다
    public String oauth2FailUrl(String reason) {
        return url + "/oauth2/fail?reason=" + reason;
    }
}
