package com.moviepick.domain.member.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberTest {

    @Test
    void 소셜_닉네임이_51자이면_50자로_잘린다() {
        // given
        String nickname = "가".repeat(51);

        // when
        Member member = Member.createSocial(
                AuthProvider.KAKAO,
                "kakao-123",
                "test@example.com",
                nickname
        );

        // then
        assertEquals(50, member.getNickname().length());
        assertEquals("가".repeat(50), member.getNickname());
    }

    @Test
    void 소셜_닉네임이_50자이면_그대로_유지된다() {
        // given
        String nickname = "가".repeat(50);

        // when
        Member member = Member.createSocial(
                AuthProvider.GOOGLE,
                "google-123",
                "test@example.com",
                nickname
        );

        // then
        assertEquals(nickname, member.getNickname());
    }

    @Test
    void 소셜_닉네임이_50자_미만이면_그대로_유지된다() {
        // given
        String nickname = "테스트회원";

        // when
        Member member = Member.createSocial(
                AuthProvider.KAKAO,
                "kakao-456",
                "test@example.com",
                nickname
        );

        // then
        assertEquals(nickname, member.getNickname());
    }
}