
package com.moviepick.domain.community.service;

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
    public void reportPost(Long reporterId, Long postId, String reason) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new IllegalArgumentException("신고할 게시글을 찾을 수 없습니다."));

        createReport(reporterId, ReportTargetType.POST, post.getId(), reason);
    }

    // 댓글 신고
    @Transactional
    public void reportComment(
            Long reporterId,
            Long commentId,
            String reason
    ) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("신고할 댓글을 찾을 수 없습니다."));

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new IllegalArgumentException("삭제된 댓글은 신고할 수 없습니다.");
        }

        if (comment.getPost().getStatus() != PostStatus.VISIBLE) {
            throw new IllegalArgumentException("삭제된 게시글의 댓글은 신고할 수 없습니다.");
        }

        createReport(
                reporterId,
                ReportTargetType.COMMENT,
                comment.getId(),
                reason
        );
    }

    // 신고 공통 처리
    private void createReport(
            Long reporterId,
            ReportTargetType targetType,
            Long targetId,
            String reason
    ) {
        if (communityReportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporterId, targetType, targetId
        )) {
            throw new IllegalArgumentException("이미 신고한 대상입니다.");
        }

        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() ->
                        new IllegalArgumentException("신고자를 찾을 수 없습니다."));

        CommunityReport report = CommunityReport.create(
                reporter, targetType, targetId, reason
        );

        communityReportRepository.save(report);
    }
}
