package com.moviepick.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.repository.MovieRepository;
import com.moviepick.domain.movie.service.MovieRatingService;
import com.moviepick.domain.review.dto.request.ReviewCreateRequest;
import com.moviepick.domain.review.entity.ReviewStatus;
import com.moviepick.domain.review.exception.ReviewErrorCode;
import com.moviepick.domain.review.repository.ReviewLikeRepository;
import com.moviepick.domain.review.repository.ReviewRepository;
import com.moviepick.global.security.RefreshTokenService;
import com.moviepick.global.config.JpaAuditingConfig;
import com.moviepick.global.exception.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 서비스 트랜잭션이 실제로 커밋·롤백되는지 보려고 테스트 트랜잭션을 끄고 H2로 확인한다. */
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({JpaAuditingConfig.class, ReviewService.class, ReviewLikeService.class,
        MovieRatingService.class, MemberService.class})
class ReviewTransactionTest {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReviewLikeService reviewLikeService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ReviewLikeRepository reviewLikeRepository;
    @MockitoSpyBean
    private ReviewRepository reviewRepository;
    @MockitoBean
    private RefreshTokenService refreshTokenService;

    private Long writerId;
    private Long likerId;
    private Long movieId;
    private Long reviewId;

    @BeforeEach
    void setUp() {
        writerId = memberRepository.save(Member.createLocal("writer@test.com", "encoded", "writer")).getId();
        likerId = memberRepository.save(Member.createLocal("liker@test.com", "encoded", "liker")).getId();
        movieId = movieRepository.save(new Movie(1L, "movie")).getId();
        reviewId = reviewService.createReview(writerId, movieId,
                new ReviewCreateRequest(new BigDecimal("4.5"), "good", false)).reviewId();
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(reviewRepository);
        reviewLikeRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("같은 회원이 두 번 좋아요하면 두 번째는 409(ALREADY_LIKED)이고 like_count는 1이다")
    void duplicateLike() {
        reviewLikeService.likeReview(likerId, reviewId);

        assertThatThrownBy(() -> reviewLikeService.likeReview(likerId, reviewId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ReviewErrorCode.ALREADY_LIKED);

        assertThat(reviewRepository.findLikeCountById(reviewId)).isEqualTo(1);
        assertThat(reviewLikeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("like_count 증가 뒤 예외가 나면 좋아요 INSERT와 증가가 함께 롤백된다")
    void likeRollsBackTogether() {
        doThrow(new IllegalStateException("boom")).when(reviewRepository).findLikeCountById(anyLong());

        assertThatThrownBy(() -> reviewLikeService.likeReview(likerId, reviewId))
                .isInstanceOf(IllegalStateException.class);

        Mockito.reset(reviewRepository);
        assertThat(reviewRepository.findLikeCountById(reviewId)).isZero();
        assertThat(reviewLikeRepository.count()).isZero();
    }

    @Test
    @DisplayName("블라인드를 두 번 해도 평점 집계는 한 번만 차감된다")
    void blindTwice() {
        reviewService.blind(reviewId);
        reviewService.blind(reviewId);

        Movie movie = movieRepository.findById(movieId).orElseThrow();
        assertThat(movie.getReviewCount()).isZero();
        assertThat(movie.getRatingSum()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reviewRepository.findById(reviewId).orElseThrow().getStatus()).isEqualTo(ReviewStatus.BLIND);
    }

    @Test
    @DisplayName("작성자가 삭제한 리뷰를 블라인드해도 평점이 다시 빠지지 않는다")
    void blindAfterDelete() {
        reviewService.deleteReview(writerId, reviewId);
        reviewService.blind(reviewId);

        Movie movie = movieRepository.findById(movieId).orElseThrow();
        assertThat(movie.getReviewCount()).isZero();
        assertThat(movie.getRatingSum()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reviewRepository.findById(reviewId).orElseThrow().getStatus()).isEqualTo(ReviewStatus.DELETED);
    }

    @Test
    @DisplayName("없는 리뷰를 블라인드하면 REVIEW_NOT_FOUND")
    void blindNotFound() {
        assertThatThrownBy(() -> reviewService.blind(reviewId + 999))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ReviewErrorCode.REVIEW_NOT_FOUND);
    }
}
