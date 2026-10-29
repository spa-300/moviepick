package com.moviepick.domain.admin.dto.response;

import com.moviepick.domain.community.entity.CommunityReport;
import com.moviepick.domain.community.entity.ReportStatus;
import com.moviepick.domain.community.entity.ReportTargetType;

public record AdminReportResponse(
        Long reportId,
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        String reason,
        String detail,
        ReportStatus status,
        String postTitle,
        String postContentPreview,
        String authorNickname,
        // 리뷰 신고 전용: 리뷰 내용 미리보기(100자), 리뷰가 달린 영화 번호
        String reviewContentPreview,
        Long movieId
) {

    public static AdminReportResponse from(CommunityReport report) {
        return new AdminReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getDetail(),
                report.getStatus(),
                null,
                null,
                null,
                null,
                null
        );
    }

    public static AdminReportResponse from(
            CommunityReport report,
            String postTitle,
            String postContentPreview,
            String authorNickname
    ) {
        return new AdminReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getDetail(),
                report.getStatus(),
                postTitle,
                postContentPreview,
                authorNickname,
                null,
                null
        );
    }

    public static AdminReportResponse forReview(
            CommunityReport report,
            String reviewContentPreview,
            String authorNickname,
            Long movieId
    ) {
        return new AdminReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getDetail(),
                report.getStatus(),
                null,
                null,
                authorNickname,
                reviewContentPreview,
                movieId
        );
    }
}
