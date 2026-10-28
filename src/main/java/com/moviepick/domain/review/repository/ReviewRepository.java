package com.moviepick.domain.review.repository;

import com.moviepick.domain.review.entity.Review;
import com.moviepick.domain.review.entity.ReviewStatus;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Collection;
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

    // 상태 전이 (조건부 UPDATE). 반영된 행이 1일 때만 평점 집계를 건드린다 → 중복 처리돼도 두 번 빠지지 않음
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Review r set r.status = :to, r.updatedAt = CURRENT_TIMESTAMP "
            + "where r.id = :id and r.status = :from")
    int changeStatus(@Param("id") Long id,
                     @Param("from") ReviewStatus from,
                     @Param("to") ReviewStatus to);

    // ===== 조회용 (N+1 방지: EntityGraph) =====

    @EntityGraph(attributePaths = "member")
    Page<Review> findByMovieIdAndStatusIn(Long movieId, Collection<ReviewStatus> statuses, Pageable pageable);

    // 전체 리뷰 목록용: 활성 영화의 리뷰만
    @EntityGraph(attributePaths = {"member", "movie"})
    Page<Review> findByStatusInAndMovieActiveTrue(Collection<ReviewStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = "movie")
    Page<Review> findByMemberIdAndStatusIn(Long memberId, Collection<ReviewStatus> statuses, Pageable pageable);

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

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Review r set r.status = :to, r.updatedAt = :now "
            + "where r.id = :id and r.status = :from")
    int changeStatus(@Param("id") Long id,
                     @Param("from") ReviewStatus from,
                     @Param("to") ReviewStatus to,
                     @Param("now") LocalDateTime now);

    // 공개 상태일 때만 +1. 바뀐 줄이 0이면 삭제·숨김된 리뷰
    @Modifying(flushAutomatically = true)
    @Query("update Review r set r.likeCount = r.likeCount + 1 "
            + "where r.id = :id and r.status = :status")
    int increaseLikeCountIfStatus(@Param("id") Long id, @Param("status") ReviewStatus status);
}