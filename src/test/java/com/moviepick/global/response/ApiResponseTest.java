package com.moviepick.global.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviepick.global.exception.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    @DisplayName("성공 응답은 success=true, code=200을 담는다")
    void okResponse() {
        ApiResponse<String> response = ApiResponse.ok("data");

        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.data()).isEqualTo("data");
    }

    @Test
    @DisplayName("오류 응답은 ErrorCode의 상태코드와 메시지를 담는다")
    void errorResponse() {
        ApiResponse<Void> response = ApiResponse.error(CommonErrorCode.UNAUTHORIZED);

        assertThat(response.success()).isFalse();
        assertThat(response.code()).isEqualTo(401);
        assertThat(response.message()).isEqualTo(CommonErrorCode.UNAUTHORIZED.getMessage());
        assertThat(response.data()).isNull();
    }
}
