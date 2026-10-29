package com.moviepick.domain.movie.controller;

import com.moviepick.domain.movie.dto.response.MovieSyncResponse;
import com.moviepick.domain.movie.service.MovieCollectionService;
import com.moviepick.global.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/movies")
public class AdminMovieController {

    private final MovieCollectionService movieCollectionService;

    // 관리자 영화 동기화 API
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<MovieSyncResponse>> syncMovies(
            @RequestParam
            @Min(1)
            @Max(5)
            int pages
    ) {

        MovieSyncResponse result =
                movieCollectionService
                        .collectPopularMoviesWithResult(pages);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
