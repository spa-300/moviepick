package com.moviepick.domain.movie.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbPopularResponse(
        Integer page,
        List<MovieSummary> results,
        @JsonProperty("total_pages")
        Integer totalPages,
        @JsonProperty("total_results")
        Integer totalResults
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MovieSummary(
            Long id,
            String title
    ) {
    }
}
