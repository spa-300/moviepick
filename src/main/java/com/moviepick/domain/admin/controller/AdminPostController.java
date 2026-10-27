package com.moviepick.domain.admin.controller;

import com.moviepick.domain.admin.service.AdminReportService;
import com.moviepick.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final AdminReportService adminReportService;

    // 관리자: 게시글 수동 숨김
    @PatchMapping("/{postId}/blind")
    public ResponseEntity<ApiResponse<Void>> blindPost(
            @PathVariable Long postId
    ) {
        adminReportService.blindPost(postId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}