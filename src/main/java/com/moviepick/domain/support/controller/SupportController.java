package com.moviepick.domain.support.controller;

import com.moviepick.domain.support.dto.request.SupportCreateRequest;
import com.moviepick.domain.support.dto.response.SupportConfirmResponse;
import com.moviepick.domain.support.dto.response.SupportCreateResponse;
import com.moviepick.domain.support.service.SupportService;
import com.moviepick.global.auth.LoginMember;
import com.moviepick.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supports")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    /** 후원 생성: 서버가 paymentId를 만들고 CREATED로 저장한다. */
    @PostMapping
    public ResponseEntity<ApiResponse<SupportCreateResponse>> createSupport(
            @LoginMember Long memberId,
            @Valid @RequestBody SupportCreateRequest request) {
        SupportCreateResponse response = supportService.createSupport(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    /** 결제 확인: 요청 본문 없이 paymentId만 받고, PortOne에 직접 조회해서 판단한다. */
    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<ApiResponse<SupportConfirmResponse>> confirm(
            @LoginMember Long memberId,
            @PathVariable String paymentId) {
        return ResponseEntity.ok(ApiResponse.ok(supportService.confirm(memberId, paymentId)));
    }
}
