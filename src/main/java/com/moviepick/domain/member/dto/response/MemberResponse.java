package com.moviepick.domain.member.dto.response;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.entity.Role;

public record MemberResponse(
        Long id,
        String email,
        String nickname,
        Role role,
        MemberStatus status
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getStatus()
        );
    }
}