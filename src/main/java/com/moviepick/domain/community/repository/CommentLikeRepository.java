
package com.moviepick.domain.community.repository;

import com.moviepick.domain.community.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 특정 회원이 특정 댓글에 좋아요를 눌렀는지 확인
    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    // 특정 회원이 특정 댓글에 누른 좋아요 조회
    Optional<CommentLike> findByCommentIdAndMemberId(
            Long commentId,
            Long memberId
    );

    // 특정 댓글의 좋아요 수 조회
    long countByCommentId(Long commentId);
}
