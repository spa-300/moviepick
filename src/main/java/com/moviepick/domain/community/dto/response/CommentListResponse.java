
package com.moviepick.domain.community.dto.response;

import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.member.entity.MemberStatus;

import java.time.LocalDateTime;

public record CommentListResponse(
        Long commentId,
        Author author,
        String content,
        long likeCount,
        boolean likedByMe,
        LocalDateTime createdAt
) {

    public record Author(
            Long memberId,
            String nickname
    ) {
    }

    public static CommentListResponse from(
            Comment comment,
            long likeCount,
            boolean likedByMe
    ) {
        return new CommentListResponse(
                comment.getId(),
                new Author(
                        comment.getMember().getId(),
                        comment.getMember().getStatus() == MemberStatus.WITHDRAWN
                                ? "탈퇴한 회원"
                                : comment.getMember().getNickname()
                ),
                comment.getContent(),
                likeCount,
                likedByMe,
                comment.getCreatedAt()
        );
    }
}
