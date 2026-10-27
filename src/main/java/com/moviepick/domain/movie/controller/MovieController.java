package com.moviepick.domain.movie.controller;

import com.moviepick.domain.movie.dto.response.MovieDetailResponse;
import com.moviepick.domain.movie.dto.response.MovieListResponse;
import com.moviepick.domain.movie.service.MovieQueryService;
import com.moviepick.domain.review.service.ReviewService;
import com.moviepick.global.auth.LoginMember;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
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
    // 내 리뷰 ID 조회를 위한 서비스
    private final ReviewService reviewService;

    // 영화 목록 조회 및 검색
    @GetMapping
    public Page<MovieListResponse> getMovies(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT
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
            @PathVariable Long movieId,
            @LoginMember(required = false) Long memberId
    ) {
        // 로그인 사용자의 해당 영화 리뷰 ID 조회
        // 비로그인 또는 작성한 리뷰가 없다면 null
        Long myReviewId = reviewService
                .findMyReviewId(memberId, movieId)
                .orElse(null);

        return movieQueryService.getMovie(
                movieId,
                myReviewId
        );
    }
}
