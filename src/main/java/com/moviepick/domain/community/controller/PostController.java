package com.moviepick.domain.community.controller;

import com.moviepick.domain.community.dto.request.PostCreateRequest;
import com.moviepick.domain.community.dto.response.PostCreateResponse;
import com.moviepick.domain.community.service.PostService;
import com.moviepick.global.auth.LoginMember;
import com.moviepick.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @LoginMember Long memberId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        PostCreateResponse response =
                postService.create(memberId, request);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.created(response));
    }
}