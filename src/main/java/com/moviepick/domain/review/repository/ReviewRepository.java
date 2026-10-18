package com.moviepick.domain.review.repository;

import com.moviepick.domain.review.entity.Review;
import com.moviepick.domain.review.entity.ReviewStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ===== 쓰기용: 집계가 어긋나지 않도록 행 잠금 =====

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Review r where r.id = :id")
    Optional<Review> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Review r where r.member.id = :memberId and r.movie.id = :movieId")
    Optional<Review> findByMemberIdAndMovieIdForUpdate(@Param("memberId") Long memberId,
                                                       @Param("movieId") Long movieId);

    // ===== 조회용 (N+1 방지: EntityGraph) =====

    @EntityGraph(attributePaths = "member")
    Page<Review> findByMovieIdAndStatus(Long movieId, ReviewStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "movie"})
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "movie")
    Page<Review> findByMemberIdAndStatus(Long memberId, ReviewStatus status, Pageable pageable);

    boolean existsByIdAndStatus(Long id, ReviewStatus status);

    // ===== 좋아요 수 (원자적 증감) =====

    @Modifying(flushAutomatically = true)
    @Query("update Review r set r.likeCount = r.likeCount + 1 where r.id = :id")
    int increaseLikeCount(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("update Review r set r.likeCount = r.likeCount - 1 where r.id = :id and r.likeCount > 0")
    int decreaseLikeCount(@Param("id") Long id);

    @Query("select r.likeCount from Review r where r.id = :id")
    int findLikeCountById(@Param("id") Long id);

    // [수정] 추가: 영화 상세의 myReviewId 조회용 (VISIBLE 리뷰만)
    @Query("select r.id from Review r "
            + "where r.member.id = :memberId and r.movie.id = :movieId and r.status = :status")
    Optional<Long> findIdByMemberIdAndMovieIdAndStatus(@Param("memberId") Long memberId,
                                                       @Param("movieId") Long movieId,
                                                       @Param("status") ReviewStatus status);
}