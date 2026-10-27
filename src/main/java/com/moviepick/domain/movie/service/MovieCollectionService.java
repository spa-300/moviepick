package com.moviepick.domain.movie.service;

import com.moviepick.domain.movie.client.TmdbClient;
import com.moviepick.domain.movie.client.dto.TmdbMovieResponse;
import com.moviepick.domain.movie.client.dto.TmdbPopularResponse;
import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

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

    // 수집 결과 반환
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

        // 중간 실패 정보를 저장
        Integer stoppedPage = null;
        String stopReason = null;

        Set<Long> processedIds = new HashSet<>();

        for (int page = 1; page <= requestedPages; page++) {

            // 1. 인기 영화 목록 조회
            TmdbPopularResponse popular;

            try {
                popular = tmdbClient.getPopularMovies(page);

                if (popular == null || popular.results() == null) {
                    throw new IllegalStateException(
                            "TMDB 인기 영화 응답이 비어 있습니다."
                    );
                }

            } catch (RestClientException | IllegalStateException e) {

                CommonErrorCode errorCode = resolveTmdbError(e);

                log.warn(
                        "TMDB 인기 영화 목록 조회 실패: page={}, reason={}",
                        page,
                        e.getMessage()
                );

                // 첫 페이지 실패 → 502 반환
                if (page == 1) {
                    throw new BusinessException(errorCode);
                }

                // 중간 페이지 실패 → 중단 정보 기록
                stoppedPage = page;
                stopReason = errorCode.getMessage();

                break;
            }

            // 정상적으로 마지막 데이터까지 조회한 경우
            if (popular.results().isEmpty()) {
                break;
            }

            // 2. 영화 상세 조회 및 저장
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

                // 동일 수집 작업 내 중복 처리 방지
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
                            page,
                            tmdbId,
                            e.getMessage()
                    );

                    // 개별 영화 실패는 건너뛰고 계속 진행
                }
            }

            log.info(
                    "영화 수집 진행: page={}, 성공={}, 실패={}",
                    page,
                    savedCount,
                    failedCount
            );

            // TMDB 마지막 페이지 확인
            if (popular.totalPages() != null
                    && page >= popular.totalPages()) {
                break;
            }
        }

        log.info(
                "영화 수집 종료: 요청={}, 성공={}, 실패={}, 중단 페이지={}, 이유={}",
                requestedPages,
                savedCount,
                failedCount,
                stoppedPage,
                stopReason
        );

        return new MovieSyncResponse(
                requestedPages,
                savedCount,
                failedCount,
                stoppedPage,
                stopReason
        );
    }

    // TMDB 예외를 공통 ErrorCode로 변환
    private CommonErrorCode resolveTmdbError(RuntimeException e) {

        // TMDB HTTP 응답 오류
        if (e instanceof RestClientResponseException responseException) {

            int status = responseException.getStatusCode().value();

            if (status == 401) {
                return CommonErrorCode.TMDB_AUTH_FAILED;
            }

            if (status == 403) {
                return CommonErrorCode.TMDB_ACCESS_DENIED;
            }
        }

        // 네트워크 오류 및 타임아웃
        if (e instanceof ResourceAccessException) {
            return CommonErrorCode.TMDB_CONNECTION_FAILED;
        }

        // 기타 TMDB API 오류
        return CommonErrorCode.TMDB_RESPONSE_FAILED;
    }
}
