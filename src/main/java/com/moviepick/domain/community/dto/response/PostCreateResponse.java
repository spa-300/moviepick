package com.moviepick.domain.community.dto.response;

import com.moviepick.domain.community.entity.Post;

public record PostCreateResponse(
        Long id,
        String title,
        String content,
        int viewCount,
        int commentCount
) {

    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getCommentCount()
        );
    }
}