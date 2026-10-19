package com.moviepick.domain.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.moviepick.domain.review.entity.Review;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 전체 리뷰 목록(author 포함), 내 리뷰(author 제외)용 */
public record ReviewWithMovieResponse(
        Long reviewId,
        ReviewMovieResponse movie,
        @JsonInclude(JsonInclude.Include.NON_NULL) AuthorResponse author,
        BigDecimal rating,
        String content,
        boolean spoiler,
        int likeCount,
        LocalDateTime createdAt
) {
    public static ReviewWithMovieResponse withAuthor(Review review) {
        return of(review, AuthorResponse.from(review.getMember()));
    }

    public static ReviewWithMovieResponse forMine(Review review) {
        return of(review, null);
    }

    private static ReviewWithMovieResponse of(Review review, AuthorResponse author) {
        return new ReviewWithMovieResponse(
                review.getId(),
                ReviewMovieResponse.from(review.getMovie()),
                author,
                review.getRating(),
                review.getContent(),
                review.isSpoiler(),
                review.getLikeCount(),
                review.getCreatedAt());
    }
}