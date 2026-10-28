package com.moviepick.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.repository.MovieRepository;
import com.moviepick.domain.movie.service.MovieRatingService;
import com.moviepick.domain.review.dto.request.ReviewCreateRequest;
import com.moviepick.domain.review.dto.request.ReviewSort;
import com.moviepick.domain.review.repository.ReviewLikeRepository;
import com.moviepick.domain.review.repository.ReviewRepository;
import com.moviepick.global.auth.RefreshTokenService;
import com.moviepick.global.config.JpaAuditingConfig;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** REVIEW-06: 숨긴 리뷰는 목록에서 자리를 유지하고 blinded=true, content·rating은 null. 평점순에서는 맨 뒤 */
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({JpaAuditingConfig.class, ReviewService.class, MovieRatingService.class, MemberService.class})
class BlindedReviewListTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Autowired
    private ReviewService reviewService;
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

    private Long movieId;
    private Long blindedWriterId;
    private Long blindedReviewId;

    @BeforeEach
    void setUp() {
        movieId = movieRepository.save(new Movie(1L, "movie")).getId();

        // 평점 5.0 리뷰를 숨긴다 → 평점 높은 순 맨 앞이 아니라 맨 뒤로 가야 한다
        blindedWriterId = saveMember("blinded");
        blindedReviewId = write(blindedWriterId, "5.0");
        write(saveMember("low"), "1.0");
        write(saveMember("mid"), "3.0");

        reviewService.blind(blindedReviewId);
    }

    @AfterEach
    void tearDown() {
        reviewLikeRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    private Long saveMember(String nickname) {
        return memberRepository.save(Member.createLocal(nickname + "@test.com", "encoded", nickname)).getId();
    }

    private Long write(Long memberId, String rating) {
        return reviewService.createReview(memberId, movieId,
                new ReviewCreateRequest(new BigDecimal(rating), "review " + rating, false)).reviewId();
    }

    private List<JsonNode> movieReviews(ReviewSort sort) {
        return toList(reviewService.getMovieReviews(movieId, null, sort, 0, 20).content());
    }

    private List<JsonNode> toList(Object content) {
        List<JsonNode> list = new ArrayList<>();
        objectMapper.valueToTree(content).forEach(list::add);
        return list;
    }

    private JsonNode find(List<JsonNode> reviews, Long reviewId) {
        return reviews.stream()
                .filter(review -> review.get("reviewId").asLong() == reviewId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("숨긴 리뷰가 목록에 없음"));
    }

    private void assertBlindedShape(JsonNode review) {
        assertThat(review.get("blinded").asBoolean()).isTrue();
        assertThat(review.get("content").isNull()).isTrue();
        assertThat(review.get("rating").isNull()).isTrue();
    }

    @Test
    @DisplayName("영화별 리뷰 목록: 숨긴 리뷰가 자리를 유지하고 blinded=true, content·rating null")
    void movieReviews_keepBlinded() {
        List<JsonNode> reviews = movieReviews(ReviewSort.LATEST);

        assertThat(reviews).hasSize(3);
        assertBlindedShape(find(reviews, blindedReviewId));
        assertThat(reviews).filteredOn(review -> review.get("reviewId").asLong() != blindedReviewId)
                .allMatch(review -> !review.get("blinded").asBoolean()
                        && !review.get("content").isNull()
                        && !review.get("rating").isNull());
    }

    @Test
    @DisplayName("평점 높은 순·낮은 순 모두 숨긴 리뷰는 맨 뒤")
    void ratingSort_blindedLast() {
        List<JsonNode> desc = movieReviews(ReviewSort.RATING_DESC);
        List<JsonNode> asc = movieReviews(ReviewSort.RATING_ASC);

        assertThat(desc.get(2).get("reviewId").asLong()).isEqualTo(blindedReviewId);
        assertThat(desc.get(0).get("rating").decimalValue()).isEqualByComparingTo("3.0");
        assertThat(asc.get(2).get("reviewId").asLong()).isEqualTo(blindedReviewId);
        assertThat(asc.get(0).get("rating").decimalValue()).isEqualByComparingTo("1.0");
    }

    @Test
    @DisplayName("전체 리뷰 목록: 숨긴 리뷰가 자리를 유지")
    void globalReviews_keepBlinded() {
        List<JsonNode> reviews = toList(reviewService.getReviews(ReviewSort.LATEST, 0, 20).content());

        assertThat(reviews).hasSize(3);
        assertBlindedShape(find(reviews, blindedReviewId));
    }

    @Test
    @DisplayName("내 리뷰 목록: 숨긴 내 리뷰도 자리를 유지")
    void myReviews_keepBlinded() {
        List<JsonNode> reviews = toList(reviewService.getMyReviews(blindedWriterId, 0, 20).content());

        assertThat(reviews).hasSize(1);
        assertBlindedShape(find(reviews, blindedReviewId));
    }

    @Test
    @DisplayName("평균 평점에서는 숨긴 리뷰가 빠진다 (기존 동작 유지)")
    void averageExcludesBlinded() {
        Movie movie = movieRepository.findById(movieId).orElseThrow();

        assertThat(movie.getReviewCount()).isEqualTo(2);
        assertThat(movie.getRatingSum()).isEqualByComparingTo("4.0");
    }
}
