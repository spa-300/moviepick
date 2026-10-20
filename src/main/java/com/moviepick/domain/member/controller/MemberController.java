package com.moviepick.domain.member.controller;

import com.moviepick.domain.member.dto.request.MemberUpdateRequest;
import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.domain.member.service.MemberService;
import com.moviepick.global.auth.LoginMember;
import com.moviepick.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
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
            @LoginMember Long memberId,
            HttpServletRequest request
    ) {
        memberService.deleteMember(memberId);

        request.getSession(false).invalidate();

        return ResponseEntity.ok(ApiResponse.ok());
    }
}