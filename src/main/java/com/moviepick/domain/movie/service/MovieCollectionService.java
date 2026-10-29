package com.moviepick.domain.movie.service;

import com.moviepick.domain.movie.client.TmdbClient;
import com.moviepick.domain.movie.client.dto.TmdbMovieResponse;
import com.moviepick.domain.movie.client.dto.TmdbPopularResponse;
import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.domain.movie.exception.MovieErrorCode;
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

    // 한 번의 요청에서 최대 5페이지까지 수집
    private static final int MAX_PAGES = 5;

    private final TmdbClient tmdbClient;
    private final MovieSaveService movieSaveService;

    // TMDB 인기 영화 수집 및 결과 반환
    public MovieSyncResponse collectPopularMoviesWithResult(
            int requestedPages
    ) {

        // 1. 수집 페이지 범위 검증
        if (requestedPages < 1 || requestedPages > MAX_PAGES) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT
            );
        }

        int savedCount = 0;
        int failedCount = 0;

        // 중간 수집 실패 정보
        Integer stoppedPage = null;
        String stopReason = null;

        // 동일 요청 내 TMDB 영화 ID 중복 처리 방지
        Set<Long> processedIds = new HashSet<>();

        // 페이지 수집 중단을 위한 바깥 반복문 라벨
        pageLoop:
        for (int page = 1; page <= requestedPages; page++) {

            TmdbPopularResponse popular;

            // 2. TMDB 인기 영화 목록 조회
            try {
                popular = tmdbClient.getPopularMovies(page);

                // 응답이 비어 있거나 형식이 잘못된 경우
                if (popular == null || popular.results() == null) {
                    throw new IllegalStateException(
                            "TMDB 인기 영화 응답이 비어 있습니다."
                    );
                }

            } catch (RestClientException | IllegalStateException e) {

                MovieErrorCode errorCode = resolveTmdbError(e);

                log.warn(
                        "TMDB 인기 영화 목록 조회 실패: page={}, reason={}",
                        page,
                        e.getMessage()
                );

                // 첫 페이지 조회 실패 시 502 반환
                if (page == 1) {
                    throw new BusinessException(errorCode);
                }

                // 중간 페이지 조회 실패 시 중단 정보 기록
                stoppedPage = page;
                stopReason = errorCode.getMessage();

                break;
            }

            // 조회할 영화가 더 없으면 수집 종료
            if (popular.results().isEmpty()) {
                break;
            }

            // 3. 영화별 상세 정보 조회 및 저장
            for (TmdbPopularResponse.MovieSummary summary
                    : popular.results()) {

                // TMDB 영화 ID 검증
                if (summary == null || summary.id() == null) {

                    failedCount++;

                    log.warn(
                            "TMDB 영화 ID가 없는 목록 항목: page={}",
                            page
                    );

                    continue;
                }

                Long tmdbId = summary.id();

                // 이미 처리한 영화는 중복 조회하지 않음
                if (!processedIds.add(tmdbId)) {
                    continue;
                }

                TmdbMovieResponse detail;

                // 4. TMDB 영화 상세 API 호출
                try {
                    detail = tmdbClient.getMovieDetail(tmdbId);

                } catch (RestClientException | IllegalStateException e) {

                    // TMDB 상세 조회에서 404가 발생한 경우
                    // 삭제되었거나 조회할 수 없는 개별 영화로 판단
                    if (e instanceof RestClientResponseException responseException
                            && responseException.getStatusCode().value() == 404) {

                        failedCount++;

                        log.warn(
                                "TMDB 영화 상세 정보 없음: page={}, tmdbId={}",
                                page,
                                tmdbId
                        );

                        // 해당 영화만 건너뛰고 다음 영화 처리
                        continue;
                    }

                    // 인증 오류, 연결 실패 등 TMDB 장애
                    MovieErrorCode errorCode = resolveTmdbError(e);

                    log.warn(
                            "TMDB 영화 상세 조회 실패: page={}, tmdbId={}, reason={}",
                            page,
                            tmdbId,
                            e.getMessage()
                    );

                    // 첫 페이지에서 발생한 TMDB 장애는 502 반환
                    if (page == 1) {
                        throw new BusinessException(errorCode);
                    }

                    // 중간 페이지에서 발생한 TMDB 장애는
                    // 해당 페이지와 이유를 기록하고 전체 수집 중단
                    stoppedPage = page;
                    stopReason = errorCode.getMessage();

                    break pageLoop;
                }

                // 5. 영화 상세 정보 DB 저장
                // TMDB 호출과 예외 처리를 분리
                try {
                    // 영화 1편 단위 트랜잭션
                    movieSaveService.saveOne(detail);

                    savedCount++;

                } catch (RuntimeException e) {

                    // DB 저장 또는 데이터 변환 실패
                    failedCount++;

                    log.warn(
                            "영화 데이터 저장 실패: page={}, tmdbId={}, reason={}",
                            page,
                            tmdbId,
                            e.getMessage()
                    );

                    // 개별 영화 저장 실패는 건너뛰고 계속 진행
                }
            }

            // 6. 페이지별 수집 결과 로그
            log.info(
                    "영화 수집 진행: page={}, 성공={}, 실패={}",
                    page,
                    savedCount,
                    failedCount
            );

            // TMDB 마지막 페이지 도달 시 종료
            if (popular.totalPages() != null
                    && page >= popular.totalPages()) {
                break;
            }
        }

        // 7. 전체 수집 결과 로그
        log.info(
                "영화 수집 종료: 요청={}, 성공={}, 실패={}, 중단 페이지={}, 이유={}",
                requestedPages,
                savedCount,
                failedCount,
                stoppedPage,
                stopReason
        );

        // 8. 관리자에게 수집 결과 반환
        return new MovieSyncResponse(
                requestedPages,
                savedCount,
                failedCount,
                stoppedPage,
                stopReason
        );
    }

    // TMDB API 호출 예외를 영화 오류 코드로 변환
    private MovieErrorCode resolveTmdbError(RuntimeException e) {

        // TMDB HTTP 응답 오류
        if (e instanceof RestClientResponseException responseException) {

            int status = responseException.getStatusCode().value();

            // 인증 토큰 오류
            if (status == 401) {
                return MovieErrorCode.TMDB_AUTH_FAILED;
            }

            // 접근 권한 오류
            if (status == 403) {
                return MovieErrorCode.TMDB_ACCESS_DENIED;
            }
        }

        // 네트워크 연결 실패 및 타임아웃
        if (e instanceof ResourceAccessException) {
            return MovieErrorCode.TMDB_CONNECTION_FAILED;
        }

        // 기타 TMDB API 응답 오류
        return MovieErrorCode.TMDB_RESPONSE_FAILED;
    }
}
