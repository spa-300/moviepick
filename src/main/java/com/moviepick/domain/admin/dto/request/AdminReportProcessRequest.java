
package com.moviepick.domain.admin.dto.request;

import com.moviepick.domain.community.entity.ReportStatus;

public record AdminReportProcessRequest(
        ReportStatus status
) {
}
