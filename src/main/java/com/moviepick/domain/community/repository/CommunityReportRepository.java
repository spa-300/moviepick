package com.moviepick.domain.community.repository;

import com.moviepick.domain.community.entity.CommunityReport;
import com.moviepick.domain.community.entity.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityReportRepository
        extends JpaRepository<CommunityReport, Long> {

    // 동일한 회원이 같은 대상을 이미 신고했는지 확인
    boolean existsByReporterIdAndTargetTypeAndTargetId(
            Long reporterId,
            ReportTargetType targetType,
            Long targetId
    );
}