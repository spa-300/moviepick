package com.moviepick.domain.admin.controller;

import com.moviepick.domain.admin.dto.request.AdminReportProcessRequest;
import com.moviepick.domain.admin.dto.response.AdminReportResponse;
import com.moviepick.domain.admin.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    // 관리자: 신고 목록 조회
    @GetMapping
    public ResponseEntity<Page<AdminReportResponse>> getReports(
            @RequestParam(defaultValue = "0") int page,
            // 범위(1~100)는 공통 페이지 규칙(PageRequestConfig)이 검사
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminReportService.getReports(page, size));
    }

    // 관리자: 신고 승인 또는 반려
    @PatchMapping("/{id}")
    public ResponseEntity<AdminReportResponse> processReport(
            @PathVariable Long id,
            @RequestBody AdminReportProcessRequest request
    ) {
        return ResponseEntity.ok(
                adminReportService.processReport(id, request)
        );
    }
}
