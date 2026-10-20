package com.moviepick.domain.community.dto.response;

import com.moviepick.domain.community.entity.Comment;

import java.time.LocalDateTime;

public record CommentCreateResponse(
        Long id,
        Long postId,
        String content,
        String nickname,
        LocalDateTime createdAt
) {
    public static CommentCreateResponse from(Comment comment) {
        return new CommentCreateResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getMember().getNickname(),
                comment.getCreatedAt()
        );
    }
}