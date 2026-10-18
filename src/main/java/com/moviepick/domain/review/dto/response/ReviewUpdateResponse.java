package com.moviepick.domain.review.dto.response;

import com.moviepick.domain.review.entity.Review;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewUpdateResponse(
        Long reviewId,
        BigDecimal rating,
        String content,
        boolean spoiler,
        LocalDateTime updatedAt
) {
    public static ReviewUpdateResponse from(Review review) {
        return new ReviewUpdateResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.isSpoiler(),
                review.getUpdatedAt());
    }
}