package com.moviepick.domain.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.moviepick.domain.review.entity.Review;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 전체 리뷰 목록(author 포함), 내 리뷰(author 제외, movieActive 포함)용 */
public record ReviewWithMovieResponse(
        Long reviewId,
        ReviewMovieResponse movie,
        @JsonInclude(JsonInclude.Include.NON_NULL) AuthorResponse author,
        BigDecimal rating,
        String content,
        boolean spoiler,
        int likeCount,
        LocalDateTime createdAt,
        // 내 리뷰 전용: 영화가 비활성이면 false (수정 버튼·영화 링크를 끌 때 사용)
        @JsonInclude(JsonInclude.Include.NON_NULL) Boolean movieActive
) {
    public static ReviewWithMovieResponse withAuthor(Review review) {
        return of(review, AuthorResponse.from(review.getMember()), null);
    }

    public static ReviewWithMovieResponse forMine(Review review) {
        return of(review, null, review.getMovie().isActive());
    }

    private static ReviewWithMovieResponse of(Review review, AuthorResponse author, Boolean movieActive) {
        return new ReviewWithMovieResponse(
                review.getId(),
                ReviewMovieResponse.from(review.getMovie()),
                author,
                review.getRating(),
                review.getContent(),
                review.isSpoiler(),
                review.getLikeCount(),
                review.getCreatedAt(),
                movieActive);
    }
}
