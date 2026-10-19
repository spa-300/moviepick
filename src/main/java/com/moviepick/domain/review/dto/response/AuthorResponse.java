package com.moviepick.domain.review.dto.response;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;

public record AuthorResponse(Long memberId, String nickname) {

    private static final String WITHDRAWN_NICKNAME = "탈퇴한 회원";

    public static AuthorResponse from(Member member) {
        String nickname = member.getStatus() == MemberStatus.WITHDRAWN
                ? WITHDRAWN_NICKNAME
                : member.getNickname();
        return new AuthorResponse(member.getId(), nickname);
    }
}