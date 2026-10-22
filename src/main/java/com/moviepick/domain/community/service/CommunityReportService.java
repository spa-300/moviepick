
package com.moviepick.domain.community.service;

import com.moviepick.domain.community.dto.response.CommunityReportResponse;
import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.community.entity.CommentStatus;
import com.moviepick.domain.community.entity.CommunityReport;
import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.entity.ReportTargetType;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.domain.community.repository.CommunityReportRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityReportService {

    private final CommunityReportRepository communityReportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    // 게시글 신고
    @Transactional
    public CommunityReportResponse reportPost(
            Long reporterId,
            Long postId,
            String reason,
            String detail
    ) {
        Post post = postRepository
                .findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        // 본인이 작성한 게시글은 신고할 수 없음
        if (post.getMember().getId().equals(reporterId)) {
            throw new BusinessException(
                    CommonErrorCode.CANNOT_REPORT_OWN_CONTENT);
        }

        return createReport(
                reporterId,
                ReportTargetType.POST,
                post.getId(),
                reason,
                detail
        );
    }

    // 댓글 신고
    @Transactional
    public CommunityReportResponse reportComment(
            Long reporterId,
            Long commentId,
            String reason,
            String detail
    ) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        if (comment.getStatus() != CommentStatus.VISIBLE
                || comment.getPost().getStatus() != PostStatus.VISIBLE) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        // 본인이 작성한 댓글은 신고할 수 없음
        if (comment.getMember().getId().equals(reporterId)) {
            throw new BusinessException(
                    CommonErrorCode.CANNOT_REPORT_OWN_CONTENT);
        }

        return createReport(
                reporterId,
                ReportTargetType.COMMENT,
                comment.getId(),
                reason,
                detail
        );
    }

    // 신고 공통 처리
    private CommunityReportResponse createReport(
            Long reporterId,
            ReportTargetType targetType,
            Long targetId,
            String reason,
            String detail
    ) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.UNAUTHORIZED));

        if (communityReportRepository
                .existsByReporterIdAndTargetTypeAndTargetId(
                        reporterId,
                        targetType,
                        targetId
                )) {
            throw new BusinessException(
                    CommonErrorCode.DUPLICATE_REPORT);
        }

        CommunityReport report;

        try {
            report = CommunityReport.create(
                    reporter,
                    targetType,
                    targetId,
                    reason,
                    detail
            );
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }

        CommunityReport savedReport =
                communityReportRepository.save(report);

        return CommunityReportResponse.from(savedReport);
    }
}
