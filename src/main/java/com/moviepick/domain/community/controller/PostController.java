package com.moviepick.domain.community.controller;

import com.moviepick.domain.community.dto.request.PostCreateRequest;
import com.moviepick.domain.community.dto.request.PostUpdateRequest;
import com.moviepick.domain.community.dto.response.PostCreateResponse;
import com.moviepick.domain.community.dto.response.PostDetailResponse;
import com.moviepick.domain.community.dto.response.PostListResponse;
import com.moviepick.domain.community.service.PostService;
import com.moviepick.global.auth.LoginMember;
import com.moviepick.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @LoginMember Long memberId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        PostCreateResponse response = postService.create(memberId, request);

        return ResponseEntity.status(201)
                .body(ApiResponse.created(response));
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getPosts(
            Pageable pageable
    ) {
        Page<PostListResponse> response = postService.getPosts(pageable);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @PathVariable Long postId
    ) {
        PostDetailResponse response = postService.getPost(postId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @LoginMember Long memberId,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        PostDetailResponse response =
                postService.updatePost(memberId, postId, request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 게시글 삭제 (논리 삭제)
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @LoginMember Long memberId,
            @PathVariable Long postId
    ) {
        postService.deletePost(memberId, postId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}