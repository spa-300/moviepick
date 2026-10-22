package com.moviepick.domain.community.dto.response;

import com.moviepick.domain.community.entity.Post;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        Author author,
        long likeCount,
        boolean likedByMe,
        int commentCount,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record Author(
            Long memberId,
            String nickname
    ) {
    }

    public static PostDetailResponse from(
            Post post,
            long likeCount,
            boolean likedByMe
    ) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                new Author(
                        post.getMember().getId(),
                        post.getMember().getNickname()
                ),
                likeCount,
                likedByMe,
                post.getCommentCount(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
