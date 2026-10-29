package com.moviepick.global.security;

import com.moviepick.global.exception.ErrorCode;

/**
 * JWT 필터가 토큰의 회원이 지금 접근할 수 있는지 확인할 때 쓰는 창구.
 * 회원 도메인이 구현하므로 보안 필터는 회원 클래스를 직접 알지 않는다.
 */
public interface MemberAccessChecker {

    // 접근 가능한 회원의 권한 이름 (ROLE_ 없이, 없으면 null)
    // 회원 없음·정지·탈퇴면 BusinessException
    String loadActiveRole(Long memberId);

    // 이 오류로 막힌 회원이면 true (그 오류 이름으로 403), 아니면 false (401)
    boolean isForbidden(ErrorCode errorCode);
}
