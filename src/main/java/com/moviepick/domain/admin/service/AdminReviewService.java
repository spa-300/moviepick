package com.moviepick.domain.admin.service;

import com.moviepick.domain.review.entity.Review;
import com.moviepick.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    // 관리자: 리뷰 삭제 (블라인드 처리)
    @Transactional
    public void deleteReview(Long reviewId) {

        // 1. 리뷰 조회 및 비관적 쓰기 잠금
        Review review = reviewRepository.findByIdForUpdate(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("리뷰를 찾을 수 없습니다.")
                );

        // 2. 관리자 블라인드 처리
        // VISIBLE 상태가 아니면 기존 Review 엔티티에서 예외 발생
        review.blind();

        // 3. @Transactional에 의해 변경 사항 자동 반영
    }
}
