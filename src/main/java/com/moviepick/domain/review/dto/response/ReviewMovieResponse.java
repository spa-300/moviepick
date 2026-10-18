package com.moviepick.domain.review.dto.response;

import com.moviepick.domain.movie.entity.Movie;

public record ReviewMovieResponse(Long movieId, String title, String posterUrl) {

    public static ReviewMovieResponse from(Movie movie) {
        return new ReviewMovieResponse(movie.getId(), movie.getTitle(), movie.getPosterUrl());
    }
}