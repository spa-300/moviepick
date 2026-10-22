package com.moviepick.domain.member.service;

import com.moviepick.domain.member.dto.request.MemberUpdateRequest;
import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.exception.MemberErrorCode;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.moviepick.global.auth.RefreshTokenService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;


    @Transactional
    public Member getActiveMember(Long memberId) {

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(
                                MemberErrorCode.MEMBER_NOT_FOUND
                        )
                );

        // 2. 정지 기간이 만료되었다면 자동 해제
        member.releaseSuspensionIfExpired();

        // 3. ACTIVE 상태가 아니면 접근 차단
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(
                    CommonErrorCode.MEMBER_SUSPENDED
            );
        }

        // 4. 정상 회원 반환
        return member;
    }


    public MemberResponse getMyInfo(Long memberId) {
        Member member = getActiveMember(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateMember(
            Long memberId,
            MemberUpdateRequest request
    ) {
        Member member = getActiveMember(memberId);

        member.updateNickname(request.nickname());

        return MemberResponse.from(member);
    }


    @Transactional
    public void deleteMember(Long memberId) {

        // 1. 회원 조회
        Member member = getActiveMember(memberId);

        // 2. 회원 탈퇴 처리
        member.withdraw();

        // 3. 기존 Refresh Token 전체 무효화
        refreshTokenService.invalidateAll(memberId);
    }


    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}