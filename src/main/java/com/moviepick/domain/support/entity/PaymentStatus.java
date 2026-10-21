package com.moviepick.domain.support.entity;

public enum PaymentStatus {
    CREATED,    // 후원 생성, 결제 전
    PAID,       // 결제 완료 (PortOne 조회로 확인됨)
    FAILED,     // 결제 실패 (금액 불일치 등)
    EXPIRED,    // 미결제 만료
    CANCELLED   // 결제 취소
}
