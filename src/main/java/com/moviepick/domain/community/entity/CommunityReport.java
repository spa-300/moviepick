package com.moviepick.domain.community.entity;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    private CommunityReport(
            Member reporter,
            ReportTargetType targetType,
            Long targetId,
            String reason
    ) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
    }

    public static CommunityReport create(
            Member reporter,
            ReportTargetType targetType,
            Long targetId,
            String reason
    ) {
        if (reporter == null) {
            throw new IllegalArgumentException("신고자는 필수입니다.");
        }

        if (targetType == null) {
            throw new IllegalArgumentException("신고 대상 유형은 필수입니다.");
        }

        if (targetId == null || targetId <= 0) {
            throw new IllegalArgumentException("신고 대상 ID가 올바르지 않습니다.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("신고 사유는 필수입니다.");
        }

        if (reason.length() > 500) {
            throw new IllegalArgumentException("신고 사유는 500자 이하여야 합니다.");
        }

        return new CommunityReport(
                reporter,
                targetType,
                targetId,
                reason.trim()
        );
    }
}