package com.moviepick.domain.movie.dto.response;

public record MovieSyncResponse(
        int requestedPages,
        int savedCount,
        int failedCount
) {
}
