package com.moviepick.domain.admin.controller;

import com.moviepick.domain.admin.service.AdminMemberService;
import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.domain.member.entity.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 관리자: 회원 목록 및 상태별 조회
    @GetMapping
    public ResponseEntity<Page<MemberResponse>> getMembers(
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<MemberResponse> members =
                adminMemberService.getMembers(status, page);

        return ResponseEntity.ok(members);
    }

    // 관리자: 회원 제재 해제
    @PatchMapping("/{memberId}/status")
    public ResponseEntity<MemberResponse> releaseSuspension(
            @PathVariable Long memberId
    ) {
        MemberResponse member =
                adminMemberService.releaseSuspension(memberId);

        return ResponseEntity.ok(member);
    }
}
