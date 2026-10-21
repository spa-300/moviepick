package com.moviepick.global.auth;

import com.moviepick.domain.member.dto.request.LoginRequest;
import com.moviepick.domain.member.dto.request.SignupRequest;
import com.moviepick.domain.member.dto.response.LoginResponse;
import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // 회원가입 API
    @PostMapping("/members")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        MemberResponse response = authService.signup(request);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.created(response));
    }

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        // 1. 로그인 처리
        LoginResult result = authService.login(request);

        // 2. Refresh Token 쿠키 생성
        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofDays(14))
                .build();

        // 3. Access Token은 JSON 응답으로 전달
        // Refresh Token은 HttpOnly 쿠키로 전달
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.toString()
                )
                .body(
                        ApiResponse.ok(result.loginResponse())
                );
    }
}
