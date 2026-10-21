package com.moviepick.domain.community.dto.response;

public record CommentLikeResponse(
        boolean liked,
        long likeCount
) {
}