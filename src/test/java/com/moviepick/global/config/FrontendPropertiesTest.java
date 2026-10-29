package com.moviepick.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// CONF-01: 프론트 주소는 app.frontend.*에서 읽고, 비어 있으면 로컬 개발 서버 주소를 쓴다
class FrontendPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfig.class);

    @EnableConfigurationProperties(FrontendProperties.class)
    static class PropertiesConfig {
    }

    @Test
    @DisplayName("값이 없으면 http://localhost:5173 기준 주소")
    void defaults() {
        FrontendProperties properties = new FrontendProperties(null, null);

        assertThat(properties.url()).isEqualTo("http://localhost:5173");
        assertThat(properties.allowedOrigins()).containsExactly("http://localhost:5173");
        assertThat(properties.oauth2SuccessUrl()).isEqualTo("http://localhost:5173/oauth2/success");
        assertThat(properties.oauth2FailUrl("OAUTH2_FAILED"))
                .isEqualTo("http://localhost:5173/oauth2/fail?reason=OAUTH2_FAILED");
    }

    @Test
    @DisplayName("allowed-origins만 비어 있으면 url을 허용 주소로 쓴다")
    void allowedOriginsFallBackToUrl() {
        FrontendProperties properties = new FrontendProperties("https://moviepick.example", List.of());

        assertThat(properties.allowedOrigins()).containsExactly("https://moviepick.example");
    }

    @Test
    @DisplayName("설정값을 읽고, allowed-origins는 쉼표로 여러 개를 받는다")
    void bindsFromProperties() {
        contextRunner
                .withPropertyValues(
                        "app.frontend.url=https://moviepick.example",
                        "app.frontend.allowed-origins=https://moviepick.example,https://admin.moviepick.example"
                )
                .run(context -> {
                    FrontendProperties properties = context.getBean(FrontendProperties.class);

                    assertThat(properties.oauth2SuccessUrl()).isEqualTo("https://moviepick.example/oauth2/success");
                    assertThat(properties.allowedOrigins())
                            .containsExactly("https://moviepick.example", "https://admin.moviepick.example");
                });
    }
}
