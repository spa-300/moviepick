package com.moviepick.domain.movie.dto.response;

import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.entity.MovieGenre;

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
        List<GenreResponse> genres
) {
    public static MovieDetailResponse from(
            Movie movie,
            List<MovieGenre> genres
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
                genreResponses
        );
    }

    public record GenreResponse(
            Integer id,
            String name
    ) {
    }
}
