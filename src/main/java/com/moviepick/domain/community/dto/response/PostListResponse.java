package com.moviepick.domain.community.dto.response;

import com.moviepick.domain.community.entity.Post;

import java.time.LocalDateTime;

public record PostListResponse(
        Long id,
        String title,
        String nickname,
        int viewCount,
        int commentCount,
        LocalDateTime createdAt
) {
    public static PostListResponse from(Post post) {
        return new PostListResponse(
                post.getId(),
                post.getTitle(),
                post.getMember().getNickname(),
                post.getViewCount(),
                post.getCommentCount(),
                post.getCreatedAt()
        );
    }
}