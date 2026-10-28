package com.moviepick.domain.review.exception;

import com.moviepick.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 리뷰를 작성한 영화입니다."),
    REVIEW_BLINDED(HttpStatus.CONFLICT, "블라인드된 리뷰가 있어 다시 작성할 수 없습니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "0.5~5.0, 0.5 단위로 입력해 주세요."),
    NOT_REVIEW_OWNER(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정·삭제할 수 있습니다."),
    CANNOT_LIKE_OWN_REVIEW(HttpStatus.BAD_REQUEST, "본인의 리뷰에는 좋아요할 수 없습니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요한 리뷰입니다."),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "정렬 기준이 올바르지 않습니다."),
    REVIEW_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요한 기록이 없습니다.");

    private final HttpStatus status;
    private final String message;
}