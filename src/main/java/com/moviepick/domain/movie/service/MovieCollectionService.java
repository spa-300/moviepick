package com.moviepick.domain.movie.service;

import com.moviepick.domain.movie.client.TmdbClient;
import com.moviepick.domain.movie.dto.response.TmdbMovieResponse;
import com.moviepick.domain.movie.dto.response.TmdbPopularResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
// TMDB 영화 데이터 수집
public class MovieCollectionService {

    private static final int MAX_PAGES = 100;

    private final TmdbClient tmdbClient;
    private final MovieSaveService movieSaveService;

    // 인기 영화 수집
    public int collectPopularMovies(int requestedPages) {

        if (requestedPages < 1 || requestedPages > MAX_PAGES) {
            throw new IllegalArgumentException(
                    "수집 페이지는 1~100 사이여야 합니다."
            );
        }

        int savedCount = 0;
        Set<Long> processedIds = new HashSet<>();

        // 1. 요청한 페이지 수만큼 반복
        for (int page = 1; page <= requestedPages; page++) {

            // 2. 인기 영화 목록 조회
            TmdbPopularResponse popular =
                    tmdbClient.getPopularMovies(page);

            if (popular.results().isEmpty()) {
                break;
            }

            // 3. 해당 페이지의 영화 순회
            for (TmdbPopularResponse.MovieSummary summary
                    : popular.results()) {

                if (summary == null || summary.id() == null) {
                    log.warn("영화 ID가 없는 목록 항목 건너뛰기");
                    continue;
                }

                Long tmdbId = summary.id();

                // 이번 수집 실행에서 중복된 영화는 건너뛰기
                if (!processedIds.add(tmdbId)) {
                    continue;
                }

                try {
                    // 4. 영화 상세 정보 조회
                    TmdbMovieResponse detail =
                            tmdbClient.getMovieDetail(tmdbId);

                    // 5. 영화 DB 저장 또는 갱신
                    movieSaveService.saveOne(detail);

                    savedCount++;

                } catch (RuntimeException e) {
                    log.error(
                            "영화 수집 실패: page={}, tmdbId={}",
                            page, tmdbId, e
                    );

                    // 오류 발생 시 이번 수집 중단
                    throw e;
                }
            }

            log.info(
                    "영화 수집 진행: page={}, 처리 성공={}편",
                    page, savedCount
            );

            // TMDB가 제공하는 마지막 페이지에 도달하면 종료
            if (popular.totalPages() != null
                    && page >= popular.totalPages()) {
                break;
            }
        }

        log.info("영화 수집 완료: 총 {}편 처리", savedCount);

        return savedCount;
    }
}
