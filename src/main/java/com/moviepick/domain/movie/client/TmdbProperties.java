package com.moviepick.domain.movie.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tmdb")
public record TmdbProperties(
        String accessToken
) {

    @Override
    public String toString() {
        return "TmdbProperties[accessToken=***]";
    }
}
