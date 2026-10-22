package com.moviepick.domain.movie.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbMovieResponse(
        Long id,
        String title,
        @JsonProperty("original_title")
        String originalTitle,
        @JsonProperty("original_language")
        String originalLanguage,
        String overview,
        @JsonProperty("release_date")
        String releaseDate,
        Integer runtime,
        String status,
        Boolean adult,
        @JsonProperty("vote_average")
        Double voteAverage,
        @JsonProperty("vote_count")
        Integer voteCount,
        Double popularity,
        @JsonProperty("poster_path")
        String posterPath,
        @JsonProperty("backdrop_path")
        String backdropPath,
        List<GenreResponse> genres
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GenreResponse(
            Integer id,
            String name
    ) {
    }
}
