package com.moviepick.domain.admin.service;

import com.moviepick.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewService reviewService;

    // 관리자: 리뷰 삭제 (블라인드 처리)
    // 상태 변경과 영화 평점 차감은 리뷰 도메인(ReviewService.blind)이 한 트랜잭션으로 처리한다
    public void deleteReview(Long reviewId) {
        reviewService.blind(reviewId);
    }
}
