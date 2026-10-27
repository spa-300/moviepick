package com.moviepick.domain.community.repository;

import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제되지 않은 게시글 목록 조회
    Page<Post> findAllByStatus(
            PostStatus status,
            Pageable pageable
    );

    // 제목 또는 본문 검색 (최신순)
    @Query("""
            SELECT p
            FROM Post p
            WHERE p.status = :status
              AND (
                  LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                  OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
              )
            """)
    Page<Post> searchByKeyword(
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 좋아요 수 기준 인기순 게시글 조회
    @Query(
            value = """
                    SELECT p
                    FROM Post p
                    LEFT JOIN PostLike pl ON pl.post = p
                    WHERE p.status = :status
                    GROUP BY p
                    ORDER BY COUNT(pl.id) DESC, p.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Post p
                    WHERE p.status = :status
                    """
    )
    Page<Post> findPopularPosts(
            @Param("status") PostStatus status,
            Pageable pageable
    );

    // 제목 또는 본문 검색 결과를 인기순으로 조회
    @Query(
            value = """
                    SELECT p
                    FROM Post p
                    LEFT JOIN PostLike pl ON pl.post = p
                    WHERE p.status = :status
                      AND (
                          LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                      )
                    GROUP BY p
                    ORDER BY COUNT(pl.id) DESC, p.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Post p
                    WHERE p.status = :status
                      AND (
                          LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                      )
                    """
    )
    Page<Post> searchPopularPosts(
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 공개 게시글과 좋아요 수를 함께 조회
    @Query(
            value = """
                    SELECT p, COUNT(pl.id)
                    FROM Post p
                    LEFT JOIN PostLike pl ON pl.post = p
                    WHERE p.status = :status
                    GROUP BY p
                    ORDER BY p.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Post p
                    WHERE p.status = :status
                    """
    )
    Page<Object[]> findAllWithLikeCountByStatus(
            @Param("status") PostStatus status,
            Pageable pageable
    );

    // 제목 또는 본문 검색 결과와 좋아요 수를 함께 조회
    @Query(
            value = """
                    SELECT p, COUNT(pl.id)
                    FROM Post p
                    LEFT JOIN PostLike pl ON pl.post = p
                    WHERE p.status = :status
                      AND (
                          LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                      )
                    GROUP BY p
                    ORDER BY p.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Post p
                    WHERE p.status = :status
                      AND (
                          LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                      )
                    """
    )
    Page<Object[]> searchWithLikeCount(
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 삭제되지 않은 게시글 상세 조회
    Optional<Post> findByIdAndStatus(
            Long id,
            PostStatus status
    );

    // 조회수 원자적 증가
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE Post p
        SET p.viewCount = p.viewCount + 1
        WHERE p.id = :id
        """)
    int increaseViewCount(@Param("id") Long id);

    // 댓글 수 원자적 증가
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE Post p
        SET p.commentCount = p.commentCount + 1
        WHERE p.id = :id
        """)
    int increaseCommentCount(@Param("id") Long id);

    // 댓글 수 원자적 감소
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE Post p
        SET p.commentCount = p.commentCount - 1
        WHERE p.id = :id
          AND p.commentCount > 0
        """)
    int decreaseCommentCount(@Param("id") Long id);
}