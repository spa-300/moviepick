package com.moviepick.domain.community.exception;

import com.moviepick.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {

    DUPLICATE_REPORT(
            HttpStatus.CONFLICT,
            "이미 신고한 대상입니다."
    ),
    // 관리자가 이미 승인·반려한 신고를 다시 처리하는 경우 (신고자 중복과 구분)
    REPORT_ALREADY_PROCESSED(
            HttpStatus.CONFLICT,
            "이미 처리된 신고입니다."
    ),
    CANNOT_REPORT_OWN_CONTENT(
            HttpStatus.FORBIDDEN,
            "본인이 작성한 게시글이나 댓글은 신고할 수 없습니다."
    );

    private final HttpStatus status;
    private final String message;
}
