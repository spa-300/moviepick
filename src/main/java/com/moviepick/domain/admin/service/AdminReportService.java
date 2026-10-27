
package com.moviepick.domain.admin.service;

import com.moviepick.domain.admin.dto.request.AdminReportProcessRequest;
import com.moviepick.domain.admin.dto.response.AdminReportResponse;
import com.moviepick.domain.admin.entity.AdminSanction;
import com.moviepick.domain.admin.repository.AdminSanctionRepository;
import com.moviepick.domain.community.entity.*;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.domain.community.repository.CommunityReportRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.entity.MemberStatus;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import com.moviepick.domain.member.entity.Role;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.moviepick.domain.community.exception.PostErrorCode;

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
    private final EntityManager entityManager;

    // 관리자: 신고 목록 조회
    public Page<AdminReportResponse> getReports(int page) {
        return communityReportRepository
                .findAll(PageRequest.of(page, 10))
                .map(report -> {
                    if (report.getTargetType() == ReportTargetType.POST) {
                        return postRepository.findById(report.getTargetId())
                                .map(post -> AdminReportResponse.from(
                                        report,
                                        post.getTitle(),
                                        createPreview(post.getContent()),
                                        post.getMember().getNickname()
                                ))
                                .orElseGet(() -> AdminReportResponse.from(report));
                    }

                    return createReportResponse(report);
                });
    }
    // 관리자: 게시글 수동 숨김
    @Transactional
    public void blindPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BusinessException(PostErrorCode.POST_NOT_FOUND));

        // 이미 삭제된 게시글은 숨김 처리하지 않음
        if (post.getStatus() == PostStatus.DELETED) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }

        post.blind();
    }

    // 관리자: 신고 승인 또는 반려
    @Transactional
    public AdminReportResponse processReport(
            Long reportId,
            AdminReportProcessRequest request
    ) {

        // 1. 요청값 검증
        if (request == null || request.status() == null) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT
            );
        }

        ReportStatus newStatus = request.status();

        // 승인 또는 반려만 허용
        if (newStatus != ReportStatus.ACCEPTED
                && newStatus != ReportStatus.REJECTED) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT
            );
        }

        // 2. 신고 조회 및 비관적 잠금
        CommunityReport report = communityReportRepository
                .findByIdForUpdate(reportId)
                .orElseThrow(() -> new BusinessException(
                        CommonErrorCode.NOT_FOUND
                ));

        // 3. 이미 처리된 신고라면 409
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(
                    CommonErrorCode.DUPLICATE_REPORT
            );
        }

        // 4. 신고 반려
        if (newStatus == ReportStatus.REJECTED) {

            int updated = communityReportRepository.updateStatusIfPending(
                    reportId,
                    ReportStatus.REJECTED,
                    ReportStatus.PENDING
            );

            if (updated == 0) {
                throw new BusinessException(
                        CommonErrorCode.DUPLICATE_REPORT
                );
            }

            // 벌크 UPDATE 결과를 관리 중인 엔티티에 반영
            entityManager.refresh(report);

            return createReportResponse(report);
        }

        // 5. 신고 대상 작성자 ID 조회
        Long memberId = findReportedMemberId(report);

        // 게시글 신고 승인 시 게시글 블라인드 처리
        if (report.getTargetType() == ReportTargetType.POST) {
            Post post = postRepository.findById(report.getTargetId())
                    .orElseThrow(() ->
                            new BusinessException(CommonErrorCode.NOT_FOUND));

            post.blind();
        }
        // TODO: 댓글 신고 승인 시 댓글 숨김 처리
        //  CommentStatus에 숨김 상태값이 없어 DB 변경이 필요하므로 이번 작업에서는 제외 (팀장 공유 필요)

        // 6. 회원 잠금
        // 같은 회원에 대한 제재 횟수 계산을 직렬화
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(
                        CommonErrorCode.NOT_FOUND
                ));

        // 관리자 또는 탈퇴 회원은 제재 대상에서 제외
        boolean skipSanction =
                member.getRole() == Role.ADMIN
                        || member.getStatus() == MemberStatus.WITHDRAWN;

        // 7. 동일 신고 대상에 제재가 있는지 확인
        boolean alreadySanctioned =
                adminSanctionRepository.existsByReportTarget(
                        report.getTargetType(),
                        report.getTargetId()
                );

        // 8. PENDING 상태의 신고만 승인
        int updated = communityReportRepository.updateStatusIfPending(
                reportId,
                ReportStatus.ACCEPTED,
                ReportStatus.PENDING
        );

        if (updated == 0) {
            throw new BusinessException(
                    CommonErrorCode.DUPLICATE_REPORT
            );
        }
        // UPDATE 결과를 현재 신고 엔티티에 반영
        entityManager.refresh(report);

        // 동일 게시글 또는 댓글의 나머지 PENDING 신고도 승인 처리
        communityReportRepository.updateAllPendingByTarget(
                report.getTargetType(),
                report.getTargetId(),
                ReportStatus.ACCEPTED,
                ReportStatus.PENDING
        );

        // 9. 동일 대상에 제재가 없다면 새로 적용
        if (!alreadySanctioned && !skipSanction) {
            // 누적 제재 횟수 계산
            long count =
                    adminSanctionRepository.countByMemberId(memberId) + 1;

            LocalDateTime suspendedUntil;

            if (count == 1) {

                // 첫 번째 제재: 1일 정지
                suspendedUntil = LocalDateTime.now().plusDays(1);

            } else if (count == 2) {

                // 두 번째 제재: 7일 정지
                suspendedUntil = LocalDateTime.now().plusDays(7);

            } else {

                // 세 번째 이상: 영구 정지
                suspendedUntil = null;
            }

            // 이미 영구 정지된 회원이라면 영구 정지 유지
            if (member.getStatus() == MemberStatus.SUSPENDED
                    && member.getSuspendedUntil() == null) {
                suspendedUntil = null;
            }

            // 회원 제재 적용
            member.suspend(suspendedUntil);

            // 제재 이력 생성
            AdminSanction sanction = AdminSanction.create(
                    member,
                    report,
                    Math.toIntExact(count),
                    suspendedUntil
            );

            // 제재 이력 저장
            adminSanctionRepository.save(sanction);
        }

        // 10. 승인 상태가 반영된 신고 응답 반환
        return createReportResponse(report);
    }

    // 신고 대상 게시글 또는 댓글 작성자 찾기
    private Long findReportedMemberId(CommunityReport report) {

        switch (report.getTargetType()) {

            case POST -> {

                Post post = postRepository
                        .findById(report.getTargetId())
                        .orElseThrow(() -> new BusinessException(
                                CommonErrorCode.NOT_FOUND
                        ));

                return post.getMember().getId();
            }

            case COMMENT -> {

                Comment comment = commentRepository
                        .findById(report.getTargetId())
                        .orElseThrow(() -> new BusinessException(
                                CommonErrorCode.NOT_FOUND
                        ));

                return comment.getMember().getId();
            }

            default -> throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT
            );
        }
    }
    // 게시글 본문을 최대 100자로 잘라 미리보기 생성
    private String createPreview(String content) {
        if (content == null) {
            return null;
        }

        if (content.length() <= 100) {
            return content;
        }

        return content.substring(0, 100);
    }

    // 신고 대상에 맞는 관리자 응답 생성
    private AdminReportResponse createReportResponse(CommunityReport report) {
        if (report.getTargetType() == ReportTargetType.POST) {
            return postRepository.findById(report.getTargetId())
                    .map(post -> AdminReportResponse.from(
                            report,
                            post.getTitle(),
                            createPreview(post.getContent()),
                            post.getMember().getNickname()
                    ))
                    .orElseGet(() -> AdminReportResponse.from(report));
        }

        return AdminReportResponse.from(report);
    }
}
