
package com.moviepick.domain.community.service;

import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.community.entity.CommentLike;
import com.moviepick.domain.community.entity.CommentStatus;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.repository.CommentLikeRepository;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
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
    public void addLike(Long memberId, Long commentId) {
        Comment comment = findVisibleComment(commentId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (commentLikeRepository.existsByCommentIdAndMemberId(commentId, memberId)) {
            throw new IllegalArgumentException("이미 좋아요를 누른 댓글입니다.");
        }

        CommentLike commentLike = CommentLike.create(comment, member);
        commentLikeRepository.save(commentLike);
    }

    // 댓글 좋아요 취소
    @Transactional
    public void removeLike(Long memberId, Long commentId) {
        findVisibleComment(commentId);

        CommentLike commentLike = commentLikeRepository
                .findByCommentIdAndMemberId(commentId, memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("등록된 좋아요가 없습니다."));

        commentLikeRepository.delete(commentLike);
    }

    // 댓글 좋아요 수 조회
    public long getLikeCount(Long commentId) {
        Comment comment = findVisibleComment(commentId);

        return commentLikeRepository.countByCommentId(comment.getId());
    }

    // 댓글 및 소속 게시글의 공개 상태 확인
    private Comment findVisibleComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new IllegalArgumentException("삭제된 댓글에는 접근할 수 없습니다.");
        }

        if (comment.getPost().getStatus() != PostStatus.VISIBLE) {
            throw new IllegalArgumentException("삭제된 게시글의 댓글에는 접근할 수 없습니다.");
        }

        return comment;
    }
}
