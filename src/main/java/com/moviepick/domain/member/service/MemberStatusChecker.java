package com.moviepick.domain.member.service;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.exception.MemberErrorCode;
import com.moviepick.global.auth.MemberAccessChecker;
import com.moviepick.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JWT 필터의 회원 상태 확인 (MemberAccessChecker 구현).
 * 정지 기간 만료 시 자동 해제를 포함한 판정은 MemberService.getActiveMember가 한다.
 */
@Component
@RequiredArgsConstructor
public class MemberStatusChecker implements MemberAccessChecker {

    private final MemberService memberService;

    @Override
    public String loadActiveRole(Long memberId) {
        Member member = memberService.getActiveMember(memberId);

        return member.getRole() == null ? null : member.getRole().name();
    }

    // 정지·탈퇴 회원은 보호 주소에서 로그인·refresh와 같은 403
    @Override
    public boolean isForbidden(ErrorCode errorCode) {
        return errorCode == MemberErrorCode.MEMBER_SUSPENDED
                || errorCode == MemberErrorCode.MEMBER_WITHDRAWN;
    }
}
