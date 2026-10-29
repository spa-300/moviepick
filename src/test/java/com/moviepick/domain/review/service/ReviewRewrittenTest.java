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
import com.moviepick.global.security.RefreshTokenService;
import com.moviepick.global.config.JpaAuditingConfig;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.Arrays;
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

/** 삭제 후 다시 쓴 리뷰는 응답에 rewritten=true, 새 리뷰는 false (REVIEW-04, A7) */
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({JpaAuditingConfig.class, ReviewService.class, ReviewLikeService.class,
        MovieRatingService.class, MemberService.class})
class ReviewRewrittenTest {

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

    private Long writerId;
    private Long otherId;
    private Long movieId;

    @BeforeEach
    void setUp() {
        writerId = memberRepository.save(Member.createLocal("writer@test.com", "encoded", "writer")).getId();
        otherId = memberRepository.save(Member.createLocal("other@test.com", "encoded", "other")).getId();
        movieId = movieRepository.save(new Movie(1L, "movie")).getId();
    }

    @AfterEach
    void tearDown() {
        reviewLikeRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("다시 쓴 리뷰: 작성 응답과 영화별·전체·내 리뷰 목록 모두 rewritten=true")
    void rewrittenReview() {
        Long reviewId = write(writerId).reviewId();
        reviewService.deleteReview(writerId, reviewId);

        Object created = write(writerId);

        assertThat(rewrittenOf(created)).isEqualTo(Boolean.TRUE);
        assertThat(rewrittenOf(only(reviewService.getMovieReviews(movieId, null, ReviewSort.LATEST, 0, 20).content())))
                .isEqualTo(Boolean.TRUE);
        assertThat(rewrittenOf(only(reviewService.getReviews(ReviewSort.LATEST, 0, 20).content())))
                .isEqualTo(Boolean.TRUE);
        assertThat(rewrittenOf(only(reviewService.getMyReviews(writerId, 0, 20).content())))
                .isEqualTo(Boolean.TRUE);
    }

    @Test
    @DisplayName("새로 쓴 리뷰: 작성 응답과 세 목록 모두 rewritten=false")
    void newReview() {
        Object created = write(otherId);

        assertThat(rewrittenOf(created)).isEqualTo(Boolean.FALSE);
        assertThat(rewrittenOf(only(reviewService.getMovieReviews(movieId, null, ReviewSort.LATEST, 0, 20).content())))
                .isEqualTo(Boolean.FALSE);
        assertThat(rewrittenOf(only(reviewService.getReviews(ReviewSort.LATEST, 0, 20).content())))
                .isEqualTo(Boolean.FALSE);
        assertThat(rewrittenOf(only(reviewService.getMyReviews(otherId, 0, 20).content())))
                .isEqualTo(Boolean.FALSE);
    }

    private com.moviepick.domain.review.dto.response.ReviewCreateResponse write(Long memberId) {
        return reviewService.createReview(memberId, movieId,
                new ReviewCreateRequest(new BigDecimal("4.0"), "good", false));
    }

    private static Object only(List<?> content) {
        assertThat(content).hasSize(1);
        return content.get(0);
    }

    // 응답 레코드의 rewritten 값 (필드가 없으면 null)
    private static Object rewrittenOf(Object response) {
        RecordComponent component = Arrays.stream(response.getClass().getRecordComponents())
                .filter(c -> c.getName().equals("rewritten"))
                .findFirst()
                .orElse(null);
        if (component == null) {
            return null;
        }
        try {
            return component.getAccessor().invoke(response);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
