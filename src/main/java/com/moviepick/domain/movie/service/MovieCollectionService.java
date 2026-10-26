package com.moviepick.domain.movie.service;

import com.moviepick.domain.movie.client.TmdbClient;
import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.domain.movie.client.dto.TmdbMovieResponse;
import com.moviepick.domain.movie.client.dto.TmdbPopularResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieCollectionService {

    private static final int MAX_PAGES = 100;

    private final TmdbClient tmdbClient;
    private final MovieSaveService movieSaveService;

    // 기존 public 메서드 유지
    public int collectPopularMovies(int requestedPages) {
        return collectPopularMoviesWithResult(requestedPages)
                .savedCount();
    }

    // 수집 결과를 성공·실패 건수와 함께 반환
    public MovieSyncResponse collectPopularMoviesWithResult(
            int requestedPages
    ) {

        if (requestedPages < 1 || requestedPages > MAX_PAGES) {
            throw new IllegalArgumentException(
                    "수집 페이지는 1~100 사이여야 합니다."
            );
        }

        int savedCount = 0;
        int failedCount = 0;

        Set<Long> processedIds = new HashSet<>();

        for (int page = 1; page <= requestedPages; page++) {

            // 1. 인기 영화 목록 호출
            TmdbPopularResponse popular;

            try {
                popular = tmdbClient.getPopularMovies(page);
            } catch (RuntimeException e) {
                log.warn(
                        "TMDB 인기 영화 목록 조회 실패: page={}, reason={}",
                        page, e.getMessage()
                );

                // 목록 조회 실패 시 지금까지의 결과 반환
                break;
            }

            if (popular == null || popular.results() == null) {
                log.warn(
                        "TMDB 인기 영화 목록 응답이 비어 있음: page={}",
                        page
                );
                break;
            }

            if (popular.results().isEmpty()) {
                break;
            }

            // 2. 영화별 상세 조회 및 DB 저장
            for (TmdbPopularResponse.MovieSummary summary
                    : popular.results()) {

                if (summary == null || summary.id() == null) {
                    log.warn(
                            "TMDB 영화 ID가 없는 목록 항목: page={}",
                            page
                    );
                    failedCount++;
                    continue;
                }

                Long tmdbId = summary.id();

                // 동일한 수집 실행에서 중복 영화 처리 방지
                if (!processedIds.add(tmdbId)) {
                    continue;
                }

                try {
                    TmdbMovieResponse detail =
                            tmdbClient.getMovieDetail(tmdbId);

                    // 영화 1편 단위 트랜잭션
                    movieSaveService.saveOne(detail);

                    savedCount++;

                } catch (RuntimeException e) {
                    failedCount++;

                    log.warn(
                            "영화 수집 실패: page={}, tmdbId={}, reason={}",
                            page, tmdbId, e.getMessage()
                    );

                    // 실패한 영화는 건너뛰고 다음 영화 진행
                }
            }

            log.info(
                    "영화 수집 진행: page={}, 성공={}, 실패={}",
                    page, savedCount, failedCount
            );

            // TMDB 마지막 페이지 확인
            if (popular.totalPages() != null
                    && page >= popular.totalPages()) {
                break;
            }
        }

        log.info(
                "영화 수집 완료: 요청 페이지={}, 성공={}, 실패={}",
                requestedPages, savedCount, failedCount
        );

        return new MovieSyncResponse(
                requestedPages,
                savedCount,
                failedCount
        );
    }
}
