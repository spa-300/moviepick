package com.moviepick.domain.member.controller;

import com.moviepick.domain.member.dto.request.MemberUpdateRequest;
import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.global.security.LoginMember;
import com.moviepick.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyInfo(
            @LoginMember Long memberId
    ) {
        MemberResponse response = memberService.getMyInfo(memberId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @LoginMember Long memberId,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        MemberResponse response = memberService.updateMember(memberId, request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }


    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
            @LoginMember Long memberId
    ) {
        // 로그인한 회원을 탈퇴 상태로 변경
        memberService.deleteMember(memberId);

        // JWT 방식이므로 HTTP 세션 무효화는 하지 않음
        return ResponseEntity.ok(ApiResponse.ok());
    }
}