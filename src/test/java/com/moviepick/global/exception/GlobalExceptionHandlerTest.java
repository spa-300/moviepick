package com.moviepick.global.exception;

import com.moviepick.global.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("데드락(락 획득 실패)은 500이 아니라 409 CONCURRENT_REQUEST")
    void deadlock_409() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleConcurrency(
                new CannotAcquireLockException("Deadlock found when trying to get lock"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().errorCode()).isEqualTo("CONCURRENT_REQUEST");
        assertThat(response.getBody().message()).doesNotContain("Deadlock");
    }

    @Test
    @DisplayName("다른 비관적 락 예외도 409")
    void pessimisticLock_409() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleConcurrency(
                new PessimisticLockingFailureException("lock wait timeout"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @DisplayName("ResponseStatusException은 그 상태 코드와 공통 오류 이름으로 응답")
    void responseStatus_keepsStatus() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "영화를 찾을 수 없습니다."));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().errorCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("영화를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예상 못 한 예외는 500, 내부 메시지는 응답에 넣지 않음")
    void unexpected_500_hidesMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(
                new IllegalStateException("secret internal detail"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).doesNotContain("secret");
    }

    @Test
    @DisplayName("도메인 오류 코드의 이름은 enum 이름 그대로")
    void businessException_usesEnumName() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(
                new BusinessException(CommonErrorCode.MOVIE_NOT_FOUND));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().errorCode()).isEqualTo("MOVIE_NOT_FOUND");
    }
}
