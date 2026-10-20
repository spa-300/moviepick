
package com.moviepick.domain.community.controller;

import com.moviepick.domain.community.dto.request.CommentCreateRequest;
import com.moviepick.domain.community.dto.response.CommentCreateResponse;
import com.moviepick.domain.community.dto.response.CommentListResponse;
import com.moviepick.domain.community.service.CommentService;
import com.moviepick.global.auth.LoginMember;
import com.moviepick.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 등록
    @PostMapping
    public ResponseEntity<ApiResponse<CommentCreateResponse>> createComment(
            @LoginMember Long memberId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentCreateResponse response =
                commentService.createComment(memberId, postId, request);

        return ResponseEntity.status(201)
                .body(ApiResponse.created(response));
    }

    // 댓글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentListResponse>>> getComments(
            @PathVariable Long postId
    ) {
        List<CommentListResponse> response =
                commentService.getComments(postId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 댓글 삭제 (논리 삭제)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @LoginMember Long memberId,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(memberId, postId, commentId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
