package com.moviepick.domain.admin.service;

import com.moviepick.domain.admin.dto.request.AdminReportProcessRequest;
import com.moviepick.domain.admin.dto.response.AdminReportResponse;
import com.moviepick.domain.admin.entity.AdminSanction;
import com.moviepick.domain.admin.repository.AdminSanctionRepository;
import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.community.entity.CommunityReport;
import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.ReportStatus;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.domain.community.repository.CommunityReportRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final CommunityReportRepository communityReportRepository;
    private final AdminSanctionRepository adminSanctionRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // 관리자: 신고 목록 조회
    public Page<AdminReportResponse> getReports(int page) {
        return communityReportRepository
                .findAll(PageRequest.of(page, 10))
                .map(AdminReportResponse::from);
    }

    // 관리자: 신고 승인 또는 반려
    @Transactional
    public AdminReportResponse processReport(
            Long reportId,
            AdminReportProcessRequest request
    ) {
        if (request == null || request.status() == null) {
            throw new IllegalArgumentException("신고 처리 상태는 필수입니다.");
        }

        // 동일 신고가 동시에 처리되지 않도록 잠금
        CommunityReport report = communityReportRepository
                .findByIdForUpdate(reportId)
                .orElseThrow(() ->
                        new IllegalArgumentException("신고를 찾을 수 없습니다.")
                );

        // 반려는 회원 제재 없이 상태만 변경
        if (request.status() == ReportStatus.REJECTED) {
            report.process(ReportStatus.REJECTED);
            return AdminReportResponse.from(report);
        }

        if (request.status() != ReportStatus.ACCEPTED) {
            throw new IllegalArgumentException("승인 또는 반려만 가능합니다.");
        }

        // 이미 처리된 신고는 승인할 수 없음
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        // 신고 대상의 작성자 ID 조회
        Long memberId = findReportedMemberId(report);

        // 동일 회원의 제재 횟수를 안전하게 계산하기 위한 잠금
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다.")
                );

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new IllegalStateException("탈퇴한 회원은 제재할 수 없습니다.");
        }

        if (adminSanctionRepository.existsByReportId(reportId)) {
            throw new IllegalStateException("이미 제재 처리된 신고입니다.");
        }

        // 기존 제재 기록 + 이번 승인 1건
        long count = adminSanctionRepository.countByMemberId(memberId) + 1;

        LocalDateTime suspendedUntil;

        if (count == 1) {
            suspendedUntil = LocalDateTime.now().plusDays(1);
        } else if (count == 2) {
            suspendedUntil = LocalDateTime.now().plusDays(7);
        } else {
            // 3회 이상은 영구 정지
            suspendedUntil = null;
        }

        // 이미 영구 정지된 회원은 영구 정지를 유지
        if (member.getStatus() == MemberStatus.SUSPENDED
                && member.getSuspendedUntil() == null) {
            suspendedUntil = null;
        }

        // 신고 승인
        report.process(ReportStatus.ACCEPTED);

        // 회원 정지
        member.suspend(suspendedUntil);

        // 제재 이력 저장
        AdminSanction sanction = AdminSanction.create(
                member,
                report,
                Math.toIntExact(count),
                suspendedUntil
        );

        adminSanctionRepository.save(sanction);

        return AdminReportResponse.from(report);
    }

    // 게시글 또는 댓글 작성자 찾기
    private Long findReportedMemberId(CommunityReport report) {

        switch (report.getTargetType()) {
            case POST -> {
                Post post = postRepository.findById(report.getTargetId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("신고 대상 게시글이 없습니다.")
                        );
                return post.getMember().getId();
            }

            case COMMENT -> {
                Comment comment = commentRepository.findById(report.getTargetId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("신고 대상 댓글이 없습니다.")
                        );
                return comment.getMember().getId();
            }

            default -> throw new IllegalArgumentException(
                    "지원하지 않는 신고 대상입니다."
            );
        }
    }
}
