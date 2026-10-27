package com.moviepick.domain.movie.dto.response;

import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.entity.MovieGenre;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MovieDetailResponse(
        Long id,
        Long tmdbId,
        String title,
        String originalTitle,
        String overview,
        LocalDate releaseDate,
        Integer runtimeMinutes,
        String status,
        boolean adult,
        Double voteAverage,
        Integer voteCount,
        Double popularity,
        String posterUrl,
        String backdropPath,
        List<GenreResponse> genres,
        // 우리 서비스 자체 평균 평점
        BigDecimal averageRating,
        // 우리 서비스 리뷰 개수
        int reviewCount,
        // 로그인 사용자가 작성한 리뷰 ID
        Long myReviewId
) {
    public static MovieDetailResponse from(
            Movie movie,
            List<MovieGenre> genres,
            Long myReviewId
    ) {
        List<GenreResponse> genreResponses = genres.stream()
                .map(genre -> new GenreResponse(
                        genre.getTmdbGenreId(),
                        genre.getGenreName()
                ))
                .toList();

        return new MovieDetailResponse(
                movie.getId(),
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getOriginalTitle(),
                movie.getOverview(),
                movie.getReleaseDate(),
                movie.getRuntimeMinutes(),
                movie.getStatus(),
                movie.isAdult(),
                movie.getVoteAverage(),
                movie.getVoteCount(),
                movie.getPopularity(),
                movie.getPosterUrl(),
                movie.getBackdropPath(),
                genreResponses,
                // 리뷰가 0건이면 null
                movie.getReviewCount() == 0
                        ? null
                        : movie.getAvgRating(),
                movie.getReviewCount(),
                // 리뷰가 없거나 비로그인이면 null
                myReviewId
        );
    }

    public record GenreResponse(
            Integer id,
            String name
    ) {
    }
}
