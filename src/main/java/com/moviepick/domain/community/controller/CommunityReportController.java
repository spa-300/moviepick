
package com.moviepick.domain.community.controller;

import com.moviepick.domain.community.dto.request.CommunityReportRequest;
import com.moviepick.domain.community.dto.response.CommunityReportResponse;
import com.moviepick.domain.community.service.CommunityReportService;
import com.moviepick.global.auth.LoginMember;
import com.moviepick.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityReportController {

    private final CommunityReportService communityReportService;

    // 게시글 신고
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<CommunityReportResponse>> reportPost(
            @LoginMember Long memberId,
            @PathVariable Long postId,
            @Valid @RequestBody CommunityReportRequest request
    ) {
        CommunityReportResponse response =
                communityReportService.reportPost(
                        memberId,
                        postId,
                        request.getReason(),
                        request.getDetail()
                );

        return ResponseEntity.status(201)
                .body(ApiResponse.created(response));
    }

    // 댓글 신고
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<CommunityReportResponse>> reportComment(
            @LoginMember Long memberId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommunityReportRequest request
    ) {
        CommunityReportResponse response =
                communityReportService.reportComment(
                        memberId,
                        commentId,
                        request.getReason(),
                        request.getDetail()
                );

        return ResponseEntity.status(201)
                .body(ApiResponse.created(response));
    }
}
