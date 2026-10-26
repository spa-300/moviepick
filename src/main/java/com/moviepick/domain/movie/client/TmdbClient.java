
package com.moviepick.domain.movie.client;

import com.moviepick.domain.movie.dto.response.TmdbMovieResponse;
import com.moviepick.domain.movie.dto.response.TmdbPopularResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class TmdbClient {

    private final RestClient restClient;

    public TmdbClient(@Value("${tmdb.access-token}") String token) {
        // 1. HTTP 연결 타임아웃 설정
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        // 2. HTTP 응답 읽기 타임아웃 설정
        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);

        factory.setReadTimeout(Duration.ofSeconds(5));

        // 3. TMDB RestClient 생성
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.themoviedb.org/3")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    // 1. 인기 영화 목록 조회
    public TmdbPopularResponse getPopularMovies(int page) {

        if (page < 1) {
            throw new IllegalArgumentException(
                    "페이지 번호는 1 이상이어야 합니다."
            );
        }

        TmdbPopularResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbPopularResponse.class);

        if (response == null || response.results() == null) {
            throw new IllegalStateException(
                    "TMDB 인기 영화 응답이 비어 있습니다."
            );
        }

        return response;
    }

    // 2. 영화 상세 정보 조회
    public TmdbMovieResponse getMovieDetail(Long tmdbId) {

        if (tmdbId == null || tmdbId <= 0) {
            throw new IllegalArgumentException(
                    "올바른 TMDB 영화 ID가 필요합니다."
            );
        }

        TmdbMovieResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movieId}")
                        .queryParam("language", "ko-KR")
                        .build(tmdbId))
                .retrieve()
                .body(TmdbMovieResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "TMDB 영화 상세 응답이 비어 있습니다."
            );
        }

        return response;
    }
}
