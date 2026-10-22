package com.moviepick.global.config;

import com.moviepick.global.auth.CustomAccessDeniedHandler;
import com.moviepick.global.auth.CustomAuthenticationEntryPoint;
import com.moviepick.global.auth.CustomOAuth2UserService;
import com.moviepick.global.auth.JwtAuthenticationFilter;
import com.moviepick.global.auth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    // 카카오 OAuth2 로그인 전용 보안 설정
    @Bean
    @Order(1)
    public SecurityFilterChain oauth2SecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .securityMatcher(
                        "/oauth2/authorization/**",
                        "/login/oauth2/code/**"
                )

                // OAuth2 인증 과정에서 세션 사용
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oauth2LoginSuccessHandler)
                );

        return http.build();
    }

    // 일반 API 및 JWT 인증 보안 설정
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // React와 Spring Boot 간 CORS 허용
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                // API는 JWT 기반으로 인증
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // CSRF 보호 설정
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler()
                        )
                )

                // 인증 실패(401), 권한 부족(403) 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // API 접근 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // CSRF 토큰 발급
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/csrf"
                        ).permitAll()

                        // 회원가입 및 로그인
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/members",
                                "/api/auth/login"
                        ).permitAll()

                        // 로그아웃 및 토큰 재발급
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/logout",
                                "/api/auth/refresh"
                        ).permitAll()

                        // 커뮤니티 게시글·댓글 조회는 비로그인 허용
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/community/posts",
                                "/api/community/posts/*",
                                "/api/community/posts/*/comments"
                        ).permitAll()

                        // 관리자 API
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // 나머지 API는 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터 등록
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );






        return http.build();
    }


    // React 개발 서버 CORS 설정
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 React 주소
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        // 허용할 요청 헤더
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "X-XSRF-TOKEN")
        );

        // 쿠키를 포함한 요청 허용
        configuration.setAllowCredentials(true);

        // 프론트엔드에서 읽을 수 있는 응답 헤더
        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // JWT 필터 중복 등록 방지
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
    jwtFilterRegistration(JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }

}
