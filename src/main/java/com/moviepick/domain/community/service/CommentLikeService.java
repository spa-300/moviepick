
package com.moviepick.domain.community.service;

import com.moviepick.domain.community.dto.response.CommentLikeResponse;
import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.community.entity.CommentLike;
import com.moviepick.domain.community.entity.CommentStatus;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.repository.CommentLikeRepository;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    // 댓글 좋아요 등록
    @Transactional
    public CommentLikeResponse addLike(Long memberId, Long commentId) {
        Comment comment = findVisibleComment(commentId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        // 이미 좋아요를 등록한 경우
        if (commentLikeRepository.existsByCommentIdAndMemberId(
                commentId, memberId)) {
            throw new BusinessException(CommonErrorCode.ALREADY_LIKED);
        }

        CommentLike commentLike = CommentLike.create(comment, member);
        commentLikeRepository.save(commentLike);

        long likeCount = commentLikeRepository.countByCommentId(commentId);

        return new CommentLikeResponse(true, likeCount);
    }

    // 댓글 좋아요 취소
    @Transactional
    public CommentLikeResponse removeLike(Long memberId, Long commentId) {
        findVisibleComment(commentId);

        CommentLike commentLike = commentLikeRepository
                .findByCommentIdAndMemberId(commentId, memberId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.LIKE_NOT_FOUND));

        commentLikeRepository.delete(commentLike);

        long likeCount = commentLikeRepository.countByCommentId(commentId);

        return new CommentLikeResponse(false, likeCount);
    }

    // 댓글 좋아요 수 조회
    public long getLikeCount(Long commentId) {
        Comment comment = findVisibleComment(commentId);

        return commentLikeRepository.countByCommentId(comment.getId());
    }

    // 특정 회원의 댓글 좋아요 여부 조회
    public boolean isLikedByMember(Long commentId, Long memberId) {
        return commentLikeRepository
                .existsByCommentIdAndMemberId(commentId, memberId);
    }

    // 댓글 및 소속 게시글의 공개 상태 확인
    private Comment findVisibleComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        if (comment.getStatus() != CommentStatus.VISIBLE
                || comment.getPost().getStatus() != PostStatus.VISIBLE) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        return comment;
    }
}
