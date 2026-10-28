package com.moviepick.domain.admin.controller;

import com.moviepick.domain.admin.service.AdminReviewService;
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

    private final AdminReviewService adminReviewService;

    // 관리자: 리뷰 숨김 (게시글 숨김과 같은 주소 모양, 반복 호출도 200)
    @PatchMapping("/{reviewId}/blind")
    public ResponseEntity<ApiResponse<Void>> blindReview(
            @PathVariable Long reviewId
    ) {
        adminReviewService.blindReview(reviewId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
