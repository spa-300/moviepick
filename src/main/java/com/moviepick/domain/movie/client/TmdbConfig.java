package com.moviepick.domain.movie.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
public class TmdbConfig {

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(3);

    private static final Duration READ_TIMEOUT =
            Duration.ofSeconds(5);

    @Bean("tmdbRestClient")
    public RestClient tmdbRestClient(TmdbProperties properties) {

        // 1. HTTP 연결 타임아웃
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();

        // 2. HTTP 응답 타임아웃
        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);

        factory.setReadTimeout(READ_TIMEOUT);

        // 3. TMDB 전용 RestClient 생성
        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.themoviedb.org/3")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + properties.accessToken()
                )
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }
}
