package com.moviepick.domain.review.service;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.domain.review.dto.response.LikeResponse;
import com.moviepick.domain.review.entity.Review;
import com.moviepick.domain.review.entity.ReviewLike;
import com.moviepick.domain.review.entity.ReviewStatus;
import com.moviepick.domain.review.exception.ReviewErrorCode;
import com.moviepick.domain.review.repository.ReviewLikeRepository;
import com.moviepick.domain.review.repository.ReviewRepository;
import com.moviepick.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewLikeService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MemberService memberService;

    @Transactional
    public LikeResponse likeReview(Long memberId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .filter(Review::isVisible)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (review.isOwnedBy(memberId)) {
            throw new BusinessException(ReviewErrorCode.CANNOT_LIKE_OWN_REVIEW);
        }

        Member member = memberService.getMember(memberId);   // [수정] getActiveMember → getMember

        // 좋아요 수 +1을 먼저 실행해 리뷰 줄 쓰기 잠금을 먼저 잡는다 (데드락 방지).
        // 공개 상태일 때만 바뀌므로, 동시에 삭제·숨김된 리뷰면 0건 → 404
        int updated = reviewRepository.increaseLikeCountIfStatus(reviewId, ReviewStatus.VISIBLE);
        if (updated == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }

        try {
            reviewLikeRepository.saveAndFlush(ReviewLike.create(review, member));
        } catch (DataIntegrityViolationException e) {
            // 중복이면 409. 앞의 +1은 같은 트랜잭션이라 함께 롤백된다
            if (isDuplicateLike(e)) {
                throw new BusinessException(ReviewErrorCode.ALREADY_LIKED);
            }
            throw e;
        }

        return new LikeResponse(true, reviewRepository.findLikeCountById(reviewId));
    }

    private boolean isDuplicateLike(DataIntegrityViolationException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve) {
                String name = cve.getConstraintName();
                return name != null && name.toLowerCase().contains(ReviewLike.UK_MEMBER_REVIEW);
            }
        }
        return false;
    }

    @Transactional
    public LikeResponse unlikeReview(Long memberId, Long reviewId) {
        if (!reviewRepository.existsByIdAndStatus(reviewId, ReviewStatus.VISIBLE)) {
            throw new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }

        // 지운 행이 없으면 좋아요 기록이 없는 것
        int deleted = reviewLikeRepository.deleteByReviewIdAndMemberId(reviewId, memberId);
        if (deleted == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_LIKE_NOT_FOUND);
        }

        reviewRepository.decreaseLikeCount(reviewId);
        return new LikeResponse(false, reviewRepository.findLikeCountById(reviewId));
    }
}