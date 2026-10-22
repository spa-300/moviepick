
package com.moviepick.domain.community.repository;

import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.community.entity.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 댓글 목록 조회 (오래된 댓글부터)
    List<Comment> findAllByPostIdAndStatusOrderByCreatedAtAsc(
            Long postId,
            CommentStatus status
    );

    // 특정 게시글의 댓글 목록 페이지 조회 (오래된 댓글부터)
    Page<Comment> findAllByPostIdAndStatusOrderByCreatedAtAsc(
            Long postId,
            CommentStatus status,
            Pageable pageable
    );

    // 댓글과 좋아요 수를 함께 조회
    @Query(
            value = """
                    SELECT c, COUNT(cl.id)
                    FROM Comment c
                    LEFT JOIN CommentLike cl ON cl.comment = c
                    WHERE c.post.id = :postId
                      AND c.status = :status
                    GROUP BY c
                    ORDER BY c.createdAt ASC
                    """,
            countQuery = """
                    SELECT COUNT(c)
                    FROM Comment c
                    WHERE c.post.id = :postId
                      AND c.status = :status
                    """
    )
    Page<Object[]> findCommentsWithLikeCount(
            @Param("postId") Long postId,
            @Param("status") CommentStatus status,
            Pageable pageable
    );
}
