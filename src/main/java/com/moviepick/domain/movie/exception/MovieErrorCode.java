package com.moviepick.domain.movie.exception;

import com.moviepick.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MovieErrorCode implements ErrorCode {

    MOVIE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 영화입니다."
    ),

    // TMDB 인증 토큰이 잘못되었거나 만료된 경우
    TMDB_AUTH_FAILED(
            HttpStatus.BAD_GATEWAY,
            "TMDB 토큰 오류"
    ),

    // TMDB 접근 권한이 거부된 경우
    TMDB_ACCESS_DENIED(
            HttpStatus.BAD_GATEWAY,
            "TMDB 접근 권한 오류"
    ),

    // TMDB 연결 실패 또는 시간 초과
    TMDB_CONNECTION_FAILED(
            HttpStatus.BAD_GATEWAY,
            "TMDB 연결 실패"
    ),

    // TMDB API 응답 오류
    TMDB_RESPONSE_FAILED(
            HttpStatus.BAD_GATEWAY,
            "TMDB 응답 오류"
    );

    private final HttpStatus status;
    private final String message;
}
