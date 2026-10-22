package com.moviepick.domain.movie.service;

import com.moviepick.domain.movie.dto.response.MovieDetailResponse;
import com.moviepick.domain.movie.dto.response.MovieListResponse;
import com.moviepick.domain.movie.entity.Movie;
import com.moviepick.domain.movie.entity.MovieGenre;
import com.moviepick.domain.movie.repository.MovieGenreRepository;
import com.moviepick.domain.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 사용자 영화 목록·상세·검색
public class MovieQueryService {

    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;

    // 영화 목록 및 제목 검색
    public Page<MovieListResponse> getMovies(
            String keyword,
            Pageable pageable
    ) {

        Page<Movie> movies;

        if (keyword == null || keyword.isBlank()) {
            movies = movieRepository.findAllByActiveTrue(pageable);
        } else {
            movies = movieRepository
                    .findByActiveTrueAndTitleContainingIgnoreCase(
                            keyword.trim(),
                            pageable
                    );
        }

        return movies.map(MovieListResponse::from);
    }

    // 영화 상세 조회
    public MovieDetailResponse getMovie(Long movieId) {

        Movie movie = movieRepository
                .findByIdAndActiveTrue(movieId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "영화를 찾을 수 없습니다."
                        )
                );

        List<MovieGenre> genres =
                movieGenreRepository.findAllByMovie_Id(movieId);

        return MovieDetailResponse.from(movie, genres);
    }
}
