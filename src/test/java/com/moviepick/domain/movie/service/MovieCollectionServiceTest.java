package com.moviepick.domain.movie.service;

import com.moviepick.domain.movie.client.TmdbClient;
import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.domain.movie.client.dto.TmdbMovieResponse;
import com.moviepick.domain.movie.client.dto.TmdbPopularResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MovieCollectionServiceTest {

    @Mock
    private TmdbClient tmdbClient;

    @Mock
    private MovieSaveService movieSaveService;

    @InjectMocks
    private MovieCollectionService movieCollectionService;

    @Test
    @DisplayName("영화 3편 중 1편이 실패해도 나머지 2편은 저장한다")
    void collectMovies_skipFailedMovie() {

        // given: 인기 영화 3편 준비
        TmdbPopularResponse popularResponse =
                new TmdbPopularResponse(
                        1,
                        List.of(
                                new TmdbPopularResponse.MovieSummary(101L, "영화 A"),
                                new TmdbPopularResponse.MovieSummary(102L, "영화 B"),
                                new TmdbPopularResponse.MovieSummary(103L, "영화 C")
                        ),
                        1,
                        3
                );

        TmdbMovieResponse movieA = createMovie(101L, "영화 A");
        TmdbMovieResponse movieB = createMovie(102L, "영화 B");
        TmdbMovieResponse movieC = createMovie(103L, "영화 C");

        // TMDB 인기 영화 목록 반환
        given(tmdbClient.getPopularMovies(1))
                .willReturn(popularResponse);

        // 영화 상세 정보 반환
        given(tmdbClient.getMovieDetail(101L))
                .willReturn(movieA);

        given(tmdbClient.getMovieDetail(102L))
                .willReturn(movieB);

        given(tmdbClient.getMovieDetail(103L))
                .willReturn(movieC);

        // 영화 A 저장 성공
        given(movieSaveService.saveOne(movieA))
                .willReturn(1L);

        // 영화 B 저장 실패
        given(movieSaveService.saveOne(movieB))
                .willThrow(new RuntimeException("DB 저장 실패"));

        // 영화 C 저장 성공
        given(movieSaveService.saveOne(movieC))
                .willReturn(3L);

        // when: 영화 수집 실행
        MovieSyncResponse result =
                movieCollectionService
                        .collectPopularMoviesWithResult(1);

        // then: 수집 결과 검증
        assertThat(result.requestedPages()).isEqualTo(1);
        assertThat(result.savedCount()).isEqualTo(2);
        assertThat(result.failedCount()).isEqualTo(1);

        // 실패한 영화 이후에도 영화 C가 처리되었는지 확인
        verify(movieSaveService).saveOne(movieC);
    }

    // 테스트용 TMDB 영화 상세 응답 생성
    private TmdbMovieResponse createMovie(
            Long tmdbId,
            String title
    ) {
        return new TmdbMovieResponse(
                tmdbId,
                title,
                title,
                "ko",
                "테스트 영화 줄거리",
                "2026-10-01",
                120,
                "Released",
                false,
                7.5,
                100,
                10.0,
                "/poster.jpg",
                "/backdrop.jpg",
                List.of()
        );
    }
}
