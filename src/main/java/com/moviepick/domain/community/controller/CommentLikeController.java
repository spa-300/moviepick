
package com.moviepick.domain.community.controller;

import com.moviepick.domain.community.service.CommentLikeService;
import com.moviepick.global.auth.LoginMember;
import com.moviepick.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/comments/{commentId}/likes")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    // 댓글 좋아요 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addLike(
            @LoginMember Long memberId,
            @PathVariable Long commentId
    ) {
        commentLikeService.addLike(memberId, commentId);

        return ResponseEntity.status(201)
                .body(ApiResponse.created(null));
    }

    // 댓글 좋아요 취소
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeLike(
            @LoginMember Long memberId,
            @PathVariable Long commentId
    ) {
        commentLikeService.removeLike(memberId, commentId);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 댓글 좋아요 수 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Long>> getLikeCount(
            @PathVariable Long commentId
    ) {
        long count = commentLikeService.getLikeCount(commentId);

        return ResponseEntity.ok(ApiResponse.ok(count));
    }
}
