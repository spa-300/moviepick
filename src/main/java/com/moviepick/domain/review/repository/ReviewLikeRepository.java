package com.moviepick.domain.review.repository;

import com.moviepick.domain.review.entity.ReviewLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);

    @Modifying(flushAutomatically = true)
    @Query("delete from ReviewLike rl where rl.review.id = :reviewId and rl.member.id = :memberId")
    int deleteByReviewIdAndMemberId(@Param("reviewId") Long reviewId, @Param("memberId") Long memberId);

    @Query("select rl.review.id from ReviewLike rl where rl.member.id = :memberId and rl.review.id in :reviewIds")
    List<Long> findLikedReviewIds(@Param("memberId") Long memberId,
                                  @Param("reviewIds") Collection<Long> reviewIds);
}