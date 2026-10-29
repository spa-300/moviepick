package com.moviepick.domain.admin.controller;

import com.moviepick.domain.review.service.ReviewService;
import com.moviepick.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    // 관리자: 리뷰 숨김 (게시글 숨김과 같은 주소 모양, 반복 호출도 200)
    // 상태 변경과 영화 평점 차감은 리뷰 도메인(ReviewService.blind)이 한 트랜잭션으로 처리한다
    @PatchMapping("/{reviewId}/blind")
    public ResponseEntity<ApiResponse<Void>> blindReview(
            @PathVariable Long reviewId
    ) {
        reviewService.blind(reviewId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
