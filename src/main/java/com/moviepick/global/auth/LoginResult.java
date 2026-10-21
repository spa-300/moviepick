package com.moviepick.global.auth;

import com.moviepick.domain.member.dto.response.LoginResponse;

public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {
}
