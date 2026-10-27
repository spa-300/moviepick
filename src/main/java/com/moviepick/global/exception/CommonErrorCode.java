package com.moviepick.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 대상을 찾을 수 없습니다."),
    ALREADY_LIKED(
            HttpStatus.CONFLICT,
            "이미 좋아요를 등록했습니다."
    ),
    LIKE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "취소할 좋아요가 없습니다."
    ),
    DUPLICATE_REPORT(
            HttpStatus.CONFLICT,
            "이미 신고한 대상입니다."
    ),
    CANNOT_REPORT_OWN_CONTENT(
            HttpStatus.FORBIDDEN,
            "본인이 작성한 게시글이나 댓글은 신고할 수 없습니다."
    ),
    MEMBER_SUSPENDED(
            HttpStatus.FORBIDDEN,
            "정지되거나 탈퇴한 회원은 이용할 수 없습니다."
    ),

    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    ),

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