package com.moviepick.global.response;

import com.moviepick.global.exception.ErrorCode;

public record ApiResponse<T>(boolean success, int code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, 200, "요청에 성공하였습니다.", data);
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, 201, "생성되었습니다.", data);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getStatus().value(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, errorCode.getStatus().value(), message, null);
    }
}
