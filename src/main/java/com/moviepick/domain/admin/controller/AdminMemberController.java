
package com.moviepick.domain.admin.controller;

import com.moviepick.domain.admin.service.AdminMemberService;
import com.moviepick.domain.member.dto.response.MemberResponse;
import com.moviepick.domain.member.entity.MemberStatus;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
@Validated
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 관리자: 회원 목록 및 상태별 조회
    @GetMapping
    public ResponseEntity<Page<MemberResponse>> getMembers(
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
            int page,
            // 범위(1~100)는 공통 페이지 규칙(PageRequestConfig)이 검사
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MemberResponse> members =
                adminMemberService.getMembers(status, page, size);

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
