package com.moviepick.domain.community.exception;

import com.moviepick.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {

    POST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "게시글을 찾을 수 없습니다."
    ),
    POST_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "게시글에 대한 권한이 없습니다."
    ),
    POST_INVALID_INPUT(
            HttpStatus.BAD_REQUEST,
            "게시글 입력값이 올바르지 않습니다."
    );

    private final HttpStatus status;
    private final String message;
}
