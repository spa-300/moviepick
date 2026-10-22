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
        try {
            reviewLikeRepository.saveAndFlush(ReviewLike.create(review, member));
        } catch (DataIntegrityViolationException e) {
            // 중복 요청: 유니크 제약(member_id, review_id)만 409로 바꾸고, 다른 제약 위반은 그대로 던진다
            if (isDuplicateLike(e)) {
                throw new BusinessException(ReviewErrorCode.ALREADY_LIKED);
            }
            throw e;
        }

        // INSERT 성공 후에만 원자적으로 +1. 이후 예외가 나면 INSERT와 함께 롤백된다 (같은 트랜잭션)
        reviewRepository.increaseLikeCount(reviewId);
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
            throw new BusinessException(ReviewErrorCode.LIKE_NOT_FOUND);
        }

        reviewRepository.decreaseLikeCount(reviewId);
        return new LikeResponse(false, reviewRepository.findLikeCountById(reviewId));
    }
}