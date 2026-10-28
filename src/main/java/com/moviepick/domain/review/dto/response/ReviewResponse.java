package com.moviepick.domain.review.dto.response;

import com.moviepick.domain.review.entity.Review;
import com.moviepick.domain.review.entity.ReviewStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 영화별 리뷰 조회용. 숨긴 리뷰는 blinded=true, rating·content는 null */
public record ReviewResponse(
        Long reviewId,
        AuthorResponse author,
        BigDecimal rating,
        String content,
        boolean spoiler,
        int likeCount,
        boolean likedByMe,
        LocalDateTime createdAt,
        boolean blinded
) {
    public static ReviewResponse of(Review review, boolean likedByMe) {
        boolean blinded = review.getStatus() == ReviewStatus.BLIND;

        return new ReviewResponse(
                review.getId(),
                AuthorResponse.from(review.getMember()),
                blinded ? null : review.getRating(),
                blinded ? null : review.getContent(),
                review.isSpoiler(),
                review.getLikeCount(),
                likedByMe,
                review.getCreatedAt(),
                blinded);
    }
}
