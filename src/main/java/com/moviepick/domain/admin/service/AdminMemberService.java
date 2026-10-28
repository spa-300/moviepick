package com.moviepick.domain.admin.service;

import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;

    // 관리자: 회원 목록 및 상태별 조회
    public Page<MemberResponse> getMembers(
            MemberStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Member> members;

        if (status == null) {
            // 전체 회원 조회
            members = memberRepository.findAll(pageable);
        } else {
            // 회원 상태별 조회
            members = memberRepository.findByStatus(status, pageable);
        }

        return members.map(MemberResponse::from);
    }

    // 관리자: 회원 제재 해제
    @Transactional
    public MemberResponse releaseSuspension(Long memberId) {

        // 회원 ID로 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다.")
                );

        // 제재 상태인지 확인
        if (member.getStatus() != MemberStatus.SUSPENDED) {
            throw new IllegalStateException("제재된 회원만 해제할 수 있습니다.");
        }

        // SUSPENDED → ACTIVE
        member.releaseSuspension();

        // 변경된 회원 정보 반환
        return MemberResponse.from(member);
    }
}
