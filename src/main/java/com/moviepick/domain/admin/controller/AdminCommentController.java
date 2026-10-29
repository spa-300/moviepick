package com.moviepick.domain.admin.controller;

import com.moviepick.domain.admin.service.AdminCommentService;
import com.moviepick.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    // 관리자: 댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id
    ) {
        adminCommentService.deleteComment(id);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
