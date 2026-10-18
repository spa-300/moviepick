package com.moviepick.domain.review.service;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.service.MovieService;
import com.moviepick.domain.review.dto.request.ReviewCreateRequest;
import com.moviepick.domain.review.dto.request.ReviewSort;
import com.moviepick.domain.review.dto.request.ReviewUpdateRequest;
import com.moviepick.domain.review.dto.response.ReviewCreateResponse;
import com.moviepick.domain.review.dto.response.ReviewResponse;
import com.moviepick.domain.review.dto.response.ReviewUpdateResponse;
import com.moviepick.domain.review.dto.response.ReviewWithMovieResponse;
import com.moviepick.domain.review.entity.Review;
import com.moviepick.domain.review.entity.ReviewStatus;
import com.moviepick.domain.review.exception.ReviewErrorCode;
import com.moviepick.domain.review.repository.ReviewLikeRepository;
import com.moviepick.domain.review.repository.ReviewRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.response.PageResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MemberService memberService;
    private final MovieService movieService;

    // ===================== 작성 =====================

    @Transactional
    public ReviewCreateResponse createReview(Long memberId, Long movieId, ReviewCreateRequest request) {
        BigDecimal rating = normalizeRating(request.rating());
        Member member = memberService.getMember(memberId);   // [수정] getActiveMember → getMember
        Movie movie = movieService.getMovie(movieId);
        boolean spoiler = request.spoilerOrDefault();

        // 같은 (회원, 영화)의 기존 리뷰 상태에 따라 분기
        Review review = reviewRepository.findByMemberIdAndMovieIdForUpdate(memberId, movieId)
                .map(existing -> rewriteIfDeleted(existing, rating, request.content(), spoiler))
                .orElseGet(() -> saveNew(member, movie, rating, request.content(), spoiler));

        movieService.applyRating(movieId, rating, 1);
        return ReviewCreateResponse.from(review);
    }

    private Review rewriteIfDeleted(Review existing, BigDecimal rating, String content, boolean spoiler) {
        switch (existing.getStatus()) {
            case VISIBLE -> throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
            case BLIND -> throw new BusinessException(ReviewErrorCode.REVIEW_BLINDED);
            default -> {
                // [수정] 이전 좋아요 기록을 지운 뒤 덮어쓴다 (likeCount 0 초기화와 기록을 일치시킴)
                reviewLikeRepository.deleteAllByReviewId(existing.getId());
                existing.rewrite(rating, content, spoiler);   // DELETED → VISIBLE
            }
        }
        return existing;
    }

    private Review saveNew(Member member, Movie movie, BigDecimal rating, String content, boolean spoiler) {
        try {
            return reviewRepository.saveAndFlush(Review.create(member, movie, rating, content, spoiler));
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 유니크 제약(member_id, movie_id)에 걸린 경우
            throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    // ===================== 조회 =====================

    public PageResponse<ReviewResponse> getMovieReviews(
            Long movieId, Long memberId, ReviewSort sort, int page, int size) {
        movieService.getMovie(movieId);   // 없으면 MOVIE_NOT_FOUND

        Page<Review> reviews = reviewRepository.findByMovieIdAndStatus(
                movieId, ReviewStatus.VISIBLE, PageRequest.of(page, size, sort.getSort()));
        Set<Long> likedIds = findLikedReviewIds(memberId, reviews.getContent());

        return PageResponse.from(reviews.map(r -> ReviewResponse.of(r, likedIds.contains(r.getId()))));
    }

    public PageResponse<ReviewWithMovieResponse> getReviews(ReviewSort sort, int page, int size) {
        Page<Review> reviews = reviewRepository.findByStatus(
                ReviewStatus.VISIBLE, PageRequest.of(page, size, sort.getSort()));
        return PageResponse.from(reviews.map(ReviewWithMovieResponse::withAuthor));
    }

    public PageResponse<ReviewWithMovieResponse> getMyReviews(Long memberId, int page, int size) {
        Page<Review> reviews = reviewRepository.findByMemberIdAndStatus(
                memberId, ReviewStatus.VISIBLE,
                PageRequest.of(page, size, ReviewSort.LATEST.getSort()));
        return PageResponse.from(reviews.map(ReviewWithMovieResponse::forMine));
    }

    /**
     * [수정] 추가: 영화 상세의 myReviewId 조립용.
     * movie 도메인이 review를 의존하지 않도록, 영화 상세는 컨트롤러(조립 클래스)에서 이 메서드를 호출한다.
     */
    public Optional<Long> findMyReviewId(Long memberId, Long movieId) {
        if (memberId == null) {
            return Optional.empty();
        }
        return reviewRepository.findIdByMemberIdAndMovieIdAndStatus(memberId, movieId, ReviewStatus.VISIBLE);
    }

    // ===================== 수정 =====================

    @Transactional
    public ReviewUpdateResponse updateReview(Long memberId, Long reviewId, ReviewUpdateRequest request) {
        BigDecimal newRating = request.rating() == null ? null : normalizeRating(request.rating());

        Review review = findReviewForUpdate(reviewId);
        review.validateModifiableBy(memberId);

        BigDecimal oldRating = review.getRating();
        review.update(newRating, request.content(), request.spoiler());

        // 평점이 바뀌면 차이만큼만 합계에 반영 (리뷰 수는 그대로)
        if (newRating != null) {
            BigDecimal delta = newRating.subtract(oldRating);
            if (delta.signum() != 0) {
                movieService.applyRating(review.getMovie().getId(), delta, 0);
            }
        }

        reviewRepository.flush();   // updatedAt 반영 후 응답 생성
        return ReviewUpdateResponse.from(review);
    }

    // ===================== 삭제 =====================

    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        Review review = findReviewForUpdate(reviewId);
        review.validateModifiableBy(memberId);
        review.delete();   // VISIBLE → DELETED

        movieService.applyRating(review.getMovie().getId(), review.getRating().negate(), -1);
    }

    /** 관리자 파트(DELETE /api/admin/reviews/{id})에서 호출. VISIBLE → BLIND */
    @Transactional
    public void blindReview(Long reviewId) {
        Review review = findReviewForUpdate(reviewId);
        review.blind();

        movieService.applyRating(review.getMovie().getId(), review.getRating().negate(), -1);
    }

    // ===================== 내부 메서드 =====================

    private Review findReviewForUpdate(Long reviewId) {
        return reviewRepository.findByIdForUpdate(reviewId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    /** 범위·단위 검증(INVALID_RATING) 후 소수 첫째 자리로 통일 */
    private BigDecimal normalizeRating(BigDecimal rating) {
        Review.validateRating(rating);
        return rating.setScale(1, RoundingMode.UNNECESSARY);
    }

    private Set<Long> findLikedReviewIds(Long memberId, List<Review> reviews) {
        if (memberId == null || reviews.isEmpty()) {
            return Set.of();
        }
        List<Long> ids = reviews.stream().map(Review::getId).toList();
        return new HashSet<>(reviewLikeRepository.findLikedReviewIds(memberId, ids));
    }
}