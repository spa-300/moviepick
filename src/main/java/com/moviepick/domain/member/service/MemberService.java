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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public Member getActiveMember(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND)
                );

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(
                    CommonErrorCode.MEMBER_SUSPENDED
            );
        }

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
        Member member = getActiveMember(memberId);

        member.withdraw();
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}