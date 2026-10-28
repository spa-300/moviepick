package com.moviepick.domain.movie.service;

import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.repository.MovieRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
// 리뷰 작성 대상 영화 확인 및 자체 평점 집계 갱신
public class MovieRatingService {

    private final MovieRepository movieRepository;

    // 리뷰 작성·목록 대상 영화 조회 (없거나 비활성이면 MOVIE_NOT_FOUND)
    @Transactional(readOnly = true)
    public Movie getMovie(Long movieId) {
        return movieRepository.findByIdAndActiveTrue(movieId)
                .orElseThrow(() ->
                        new BusinessException(
                                CommonErrorCode.MOVIE_NOT_FOUND
                        )
                );
    }

    // 리뷰 평점 집계 증감
    @Transactional
    public void applyRating(
            Long movieId,
            BigDecimal ratingDelta,
            int countDelta
    ) {
        int updated = movieRepository.applyRating(
                movieId,
                ratingDelta,
                countDelta
        );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "영화 평점 집계를 갱신할 수 없습니다. ID: " + movieId
            );
        }
    }
}
