package com.moviepick.domain.member.dto.response;


public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {
}
