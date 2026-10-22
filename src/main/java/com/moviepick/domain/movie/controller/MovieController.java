package com.moviepick.domain.movie.controller;

import com.moviepick.domain.movie.dto.response.MovieDetailResponse;
import com.moviepick.domain.movie.dto.response.MovieListResponse;
import com.moviepick.domain.movie.service.MovieQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieQueryService movieQueryService;

    // 영화 목록 조회 및 검색
    @GetMapping
    public Page<MovieListResponse> getMovies(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "페이지는 0 이상, 크기는 1~100이어야 합니다."
            );
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return movieQueryService.getMovies(keyword, pageable);
    }

    // 영화 상세 조회
    @GetMapping("/{movieId}")
    public MovieDetailResponse getMovie(
            @PathVariable Long movieId
    ) {
        return movieQueryService.getMovie(movieId);
    }
}
