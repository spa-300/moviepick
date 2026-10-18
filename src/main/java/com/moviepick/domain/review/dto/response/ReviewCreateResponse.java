package com.moviepick.domain.review.dto.response;

import com.moviepick.domain.review.entity.Review;
import com.moviepick.domain.review.entity.ReviewStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewCreateResponse(
        Long reviewId,
        Long movieId,
        BigDecimal rating,
        String content,
        boolean spoiler,
        ReviewStatus status,
        LocalDateTime createdAt
) {
    public static ReviewCreateResponse from(Review review) {
        return new ReviewCreateResponse(
                review.getId(),
                review.getMovie().getId(),
                review.getRating(),
                review.getContent(),
                review.isSpoiler(),
                review.getStatus(),
                review.getCreatedAt());
    }
}