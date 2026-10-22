package com.moviepick.domain.movie.dto.response;

import com.moviepick.domain.movie.entity.Movie;

import java.time.LocalDate;

public record MovieListResponse(
        Long id,
        String title,
        LocalDate releaseDate,
        Double voteAverage,
        String posterPath
) {
    public static MovieListResponse from(Movie movie) {
        return new MovieListResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getReleaseDate(),
                movie.getVoteAverage(),
                movie.getPosterPath()
        );
    }
}
