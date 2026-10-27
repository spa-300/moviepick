
package com.moviepick.domain.admin.dto.response;

import com.moviepick.domain.community.entity.CommunityReport;
import com.moviepick.domain.community.entity.ReportStatus;
import com.moviepick.domain.community.entity.ReportTargetType;

public record AdminReportResponse(
        Long id,
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        String reason,
        String detail,
        ReportStatus status,
        String postTitle,
        String postContentPreview,
        String authorNickname
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
                authorNickname
        );
    }
}
