package com.moviepick.domain.member.service;

import com.moviepick.domain.member.dto.request.MemberUpdateRequest;
import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.exception.MemberErrorCode;
import com.moviepick.domain.member.repository.MemberConstraints;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.moviepick.global.security.RefreshTokenService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;



    @Transactional
    public Member getActiveMember(Long memberId) {

        // 1. 회원 조회 (존재하지 않으면 404)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(
                                MemberErrorCode.MEMBER_NOT_FOUND
                        )
                );

        // 2. 정지 기간이 만료되었다면 자동 해제
        member.releaseSuspensionIfExpired();

        // 3. 정지된 회원은 접근 차단 (403)
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(
                    MemberErrorCode.MEMBER_SUSPENDED
            );
        }

        // 4. 탈퇴한 회원은 접근 차단 (403)
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(
                    MemberErrorCode.MEMBER_WITHDRAWN
            );
        }

        // 5. 정상 회원 반환
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

        // 다른 회원이 쓰는 닉네임이면 409 (자기 닉네임은 허용)
        if (memberRepository.existsByNicknameAndIdNot(
                request.nickname(),
                memberId
        )) {
            throw new BusinessException(
                    MemberErrorCode.DUPLICATE_NICKNAME
            );
        }

        member.updateNickname(request.nickname());

        try {
            // 커밋 때가 아니라 지금 UPDATE를 실행해 동시 수정의 닉네임 UNIQUE 충돌을 여기서 확인
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            if (MemberConstraints.isDuplicateNickname(e)) {
                throw new BusinessException(
                        MemberErrorCode.DUPLICATE_NICKNAME
                );
            }

            throw e;
        }

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
                        new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND)
                );

        // 제재 상태인지 확인
        if (member.getStatus() != MemberStatus.SUSPENDED) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_SUSPENDED);
        }

        // SUSPENDED → ACTIVE
        member.releaseSuspension();

        // 변경된 회원 정보 반환
        return MemberResponse.from(member);
    }
}