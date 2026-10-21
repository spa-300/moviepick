package com.moviepick.domain.community.controller;

import com.moviepick.domain.community.service.PostLikeService;
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
@RequestMapping("/api/community/posts/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 게시글 좋아요 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addLike(
            @LoginMember Long memberId,
            @PathVariable Long postId
    ) {
        postLikeService.addLike(memberId, postId);

        return ResponseEntity.status(201)
                .body(ApiResponse.created(null));
    }

    // 게시글 좋아요 취소
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeLike(
            @LoginMember Long memberId,
            @PathVariable Long postId
    ) {
        postLikeService.removeLike(memberId, postId);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 게시글 좋아요 수 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Long>> getLikeCount(
            @PathVariable Long postId
    ) {
        long count = postLikeService.getLikeCount(postId);

        return ResponseEntity.ok(ApiResponse.ok(count));
    }
}