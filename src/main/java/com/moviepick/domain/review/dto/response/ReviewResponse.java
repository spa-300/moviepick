package com.moviepick.domain.review.dto.response;

import com.moviepick.domain.review.entity.Review;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 영화별 리뷰 조회용 */
public record ReviewResponse(
        Long reviewId,
        AuthorResponse author,
        BigDecimal rating,
        String content,
        boolean spoiler,
        int likeCount,
        boolean likedByMe,
        LocalDateTime createdAt
) {
    public static ReviewResponse of(Review review, boolean likedByMe) {
        return new ReviewResponse(
                review.getId(),
                AuthorResponse.from(review.getMember()),
                review.getRating(),
                review.getContent(),
                review.isSpoiler(),
                review.getLikeCount(),
                likedByMe,
                review.getCreatedAt());
    }
}