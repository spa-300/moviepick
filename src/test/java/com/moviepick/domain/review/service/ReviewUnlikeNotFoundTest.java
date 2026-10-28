package com.moviepick.domain.review.service;

import com.moviepick.domain.member.service.MemberService;
import com.moviepick.domain.review.entity.ReviewStatus;
import com.moviepick.domain.review.repository.ReviewLikeRepository;
import com.moviepick.domain.review.repository.ReviewRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

// REVIEW-01: 좋아요하지 않은 리뷰의 좋아요 취소는 404 REVIEW_LIKE_NOT_FOUND
class ReviewUnlikeNotFoundTest {

    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final ReviewLikeRepository reviewLikeRepository = mock(ReviewLikeRepository.class);

    private final ReviewLikeService reviewLikeService = new ReviewLikeService(
            reviewRepository, reviewLikeRepository, mock(MemberService.class));

    @Test
    @DisplayName("좋아요 기록 없이 취소 → 404 REVIEW_LIKE_NOT_FOUND")
    void unlikeWithoutLike() {
        given(reviewRepository.existsByIdAndStatus(5L, ReviewStatus.VISIBLE)).willReturn(true);
        given(reviewLikeRepository.deleteByReviewIdAndMemberId(5L, 1L)).willReturn(0);

        Throwable thrown = catchThrowable(() -> reviewLikeService.unlikeReview(1L, 5L));

        assertThat(thrown).isInstanceOf(BusinessException.class);
        ErrorCode errorCode = ((BusinessException) thrown).getErrorCode();
        assertThat(errorCode.toString()).isEqualTo("REVIEW_LIKE_NOT_FOUND");
        assertThat(errorCode.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
