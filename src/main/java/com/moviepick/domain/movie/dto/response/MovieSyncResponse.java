package com.moviepick.domain.movie.dto.response;

public record MovieSyncResponse(
        int requestedPages,
        int savedCount,
        int failedCount,
        // 수집이 중단된 페이지
        Integer stoppedPage,
        // 수집 중단 이유
        String stopReason
) {
    // 기존 3개 인자 호출부 및 테스트 코드와 호환
    public MovieSyncResponse(
            int requestedPages,
            int savedCount,
            int failedCount
    ) {
        this(
                requestedPages,
                savedCount,
                failedCount,
                null,
                null
        );
    }
}
