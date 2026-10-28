package com.moviepick.domain.admin.service;

import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.community.entity.CommentStatus;
import com.moviepick.domain.community.exception.CommentErrorCode;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCommentService {

    private final CommentRepository commentRepository;

    // 관리자: 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {

        // 1. 댓글 ID로 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND)
                );

        // 2. 이미 삭제된 댓글은 없는 댓글과 같이 404
        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }

        // 3. 댓글 논리 삭제
        comment.delete();

        // 4. 게시글 댓글 수 감소
        comment.getPost().decreaseCommentCount();
    }
}
