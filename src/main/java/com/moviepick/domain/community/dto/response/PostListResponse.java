
package com.moviepick.domain.community.dto.response;

import com.moviepick.domain.community.entity.Post;

import java.time.LocalDateTime;

public record PostListResponse(
        Long postId,
        String title,
        Author author,
        long likeCount,
        int commentCount,
        int viewCount,
        LocalDateTime createdAt
) {
    public record Author(
            Long memberId,
            String nickname
    ) {
    }

    public static PostListResponse from(Post post, long likeCount) {
        return new PostListResponse(
                post.getId(),
                post.getTitle(),
                new Author(
                        post.getMember().getId(),
                        post.getMember().getNickname()
                ),
                likeCount,
                post.getCommentCount(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
