package com.moviepick.domain.community.dto.response;

import com.moviepick.domain.community.entity.CommunityReport;

public record CommunityReportResponse(
        Long reportId,
        String status
) {
    public static CommunityReportResponse from(CommunityReport report) {
        return new CommunityReportResponse(
                report.getId(),
                report.getStatus().name()
        );
    }
}