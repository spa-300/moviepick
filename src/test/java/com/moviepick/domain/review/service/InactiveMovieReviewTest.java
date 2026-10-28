package com.moviepick.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.repository.MovieRepository;
import com.moviepick.domain.movie.service.MovieQueryService;
import com.moviepick.domain.movie.service.MovieRatingService;
import com.moviepick.domain.review.dto.request.ReviewCreateRequest;
import com.moviepick.domain.review.dto.request.ReviewSort;
import com.moviepick.domain.review.dto.request.ReviewUpdateRequest;
import com.moviepick.domain.review.dto.response.ReviewWithMovieResponse;
import com.moviepick.domain.review.repository.ReviewLikeRepository;
import com.moviepick.domain.review.repository.ReviewRepository;
import com.moviepick.global.auth.RefreshTokenService;
import com.moviepick.global.config.JpaAuditingConfig;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import com.moviepick.global.response.PageResponse;
import java.math.BigDecimal;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** REVIEW-02: 없는 영화·비활성 영화는 MOVIE_NOT_FOUND. 비활성 영화의 기존 리뷰는 수정 404, 삭제 허용 */
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({JpaAuditingConfig.class, ReviewService.class, MovieRatingService.class,
        MovieQueryService.class, MemberService.class})
class InactiveMovieReviewTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private MovieQueryService movieQueryService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewLikeRepository reviewLikeRepository;
    @MockitoBean
    private RefreshTokenService refreshTokenService;

    private Long writerId;
    private Long activeMovieId;
    private Long inactiveMovieId;
    private Long inactiveReviewId;

    @BeforeEach
    void setUp() {
        writerId = memberRepository.save(Member.createLocal("writer@test.com", "encoded", "writer")).getId();
        activeMovieId = movieRepository.save(new Movie(1L, "active")).getId();
        inactiveMovieId = movieRepository.save(new Movie(2L, "inactive")).getId();

        reviewService.createReview(writerId, activeMovieId,
                new ReviewCreateRequest(new BigDecimal("4.0"), "active review", false));
        inactiveReviewId = reviewService.createReview(writerId, inactiveMovieId,
                new ReviewCreateRequest(new BigDecimal("3.0"), "inactive review", false)).reviewId();

        // 리뷰를 쓴 뒤 영화가 비활성으로 바뀐 상황
        Movie inactive = movieRepository.findById(inactiveMovieId).orElseThrow();
        ReflectionTestUtils.setField(inactive, "active", false);
        movieRepository.save(inactive);
    }

    @AfterEach
    void tearDown() {
        reviewLikeRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    private void assertMovieNotFound(ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CommonErrorCode.MOVIE_NOT_FOUND);
    }

    @Test
    @DisplayName("없는 영화 상세 → MOVIE_NOT_FOUND")
    void missingMovieDetail() {
        assertMovieNotFound(() -> movieQueryService.getMovie(activeMovieId + 999, null));
    }

    @Test
    @DisplayName("비활성 영화 리뷰 목록 → MOVIE_NOT_FOUND")
    void inactiveMovieReviews() {
        assertMovieNotFound(() -> reviewService.getMovieReviews(
                inactiveMovieId, null, ReviewSort.LATEST, 0, 20));
    }

    @Test
    @DisplayName("비활성 영화 리뷰 작성 → MOVIE_NOT_FOUND")
    void inactiveMovieCreate() {
        Long otherId = memberRepository.save(Member.createLocal("other@test.com", "encoded", "other")).getId();

        assertMovieNotFound(() -> reviewService.createReview(otherId, inactiveMovieId,
                new ReviewCreateRequest(new BigDecimal("5.0"), "new", false)));
    }

    @Test
    @DisplayName("비활성 영화의 기존 리뷰 수정 → MOVIE_NOT_FOUND")
    void inactiveMovieUpdate() {
        assertMovieNotFound(() -> reviewService.updateReview(writerId, inactiveReviewId,
                new ReviewUpdateRequest(new BigDecimal("1.0"), "changed", null)));
    }

    @Test
    @DisplayName("비활성 영화의 기존 리뷰 삭제 → 성공, 평점 집계 차감")
    void inactiveMovieDelete() {
        reviewService.deleteReview(writerId, inactiveReviewId);

        Movie movie = movieRepository.findById(inactiveMovieId).orElseThrow();
        assertThat(movie.getReviewCount()).isZero();
    }

    @Test
    @DisplayName("전체 리뷰 목록에서 비활성 영화의 리뷰는 빠진다")
    void globalListExcludesInactive() {
        PageResponse<ReviewWithMovieResponse> page = reviewService.getReviews(ReviewSort.LATEST, 0, 20);

        assertThat(page.content()).extracting(r -> r.movie().movieId()).containsExactly(activeMovieId);
    }

    @Test
    @DisplayName("내 리뷰 목록에는 남고, movieActive로 비활성 영화를 알 수 있다")
    void myReviewsKeepInactiveWithFlag() {
        PageResponse<ReviewWithMovieResponse> page = reviewService.getMyReviews(writerId, 0, 20);
        JsonNode content = objectMapper.valueToTree(page.content());

        assertThat(content).hasSize(2);
        for (JsonNode review : content) {
            long movieId = review.get("movie").get("movieId").asLong();
            assertThat(review.has("movieActive")).isTrue();
            assertThat(review.get("movieActive").asBoolean()).isEqualTo(movieId == activeMovieId);
        }
    }

    @Test
    @DisplayName("전체 리뷰 목록 응답에는 movieActive가 나가지 않는다")
    void globalListHasNoMovieActive() {
        JsonNode content = objectMapper.valueToTree(
                reviewService.getReviews(ReviewSort.LATEST, 0, 20).content());

        assertThat(List.copyOf(content.valueStream().toList()))
                .allMatch(review -> !review.has("movieActive"));
    }
}
