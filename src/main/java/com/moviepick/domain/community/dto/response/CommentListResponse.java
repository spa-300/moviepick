package com.moviepick.domain.community.dto.response;

import com.moviepick.domain.community.entity.Comment;

import java.time.LocalDateTime;

public record CommentListResponse(
        Long id,
        Long memberId,
        String nickname,
        String content,
        LocalDateTime createdAt
) {
    public static CommentListResponse from(Comment comment) {
        return new CommentListResponse(
                comment.getId(),
                comment.getMember().getId(),
                comment.getMember().getNickname(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}