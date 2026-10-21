
package com.moviepick.domain.community.service;

import com.moviepick.domain.community.dto.request.CommentCreateRequest;
import com.moviepick.domain.community.dto.request.CommentUpdateRequest;
import com.moviepick.domain.community.dto.response.CommentCreateResponse;
import com.moviepick.domain.community.dto.response.CommentListResponse;
import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.community.entity.CommentStatus;
import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentLikeService commentLikeService;

    // 댓글 등록
    @Transactional
    public CommentCreateResponse createComment(
            Long memberId,
            Long postId,
            CommentCreateRequest request
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        Post post = postRepository
                .findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        Comment comment = Comment.create(post, member, request.content());
        Comment savedComment = commentRepository.save(comment);

        post.increaseCommentCount();

        return CommentCreateResponse.from(savedComment);
    }

    // 댓글 목록 조회
    public Page<CommentListResponse> getComments(
            Long postId,
            Long memberId,
            Pageable pageable
    ) {
        postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        Pageable pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return commentRepository.findCommentsWithLikeCount(
                postId,
                CommentStatus.VISIBLE,
                pageRequest
        ).map(row -> {
            Comment comment = (Comment) row[0];
            long likeCount = ((Number) row[1]).longValue();

            boolean likedByMe = memberId != null
                    && commentLikeService.isLikedByMember(
                    comment.getId(),
                    memberId
            );

            return CommentListResponse.from(
                    comment,
                    likeCount,
                    likedByMe
            );
        });
    }

    // 댓글 삭제 (게시글 ID와 댓글 ID 기준)
    @Transactional
    public void deleteComment(
            Long memberId,
            Long postId,
            Long commentId
    ) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        comment.delete();
        comment.getPost().decreaseCommentCount();
    }

    // 댓글 삭제 (댓글 ID 기준)
    @Transactional
    public void deleteCommentById(Long memberId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        comment.delete();
        comment.getPost().decreaseCommentCount();
    }

    // 댓글 수정
    @Transactional
    public CommentCreateResponse updateComment(
            Long memberId,
            Long postId,
            Long commentId,
            CommentUpdateRequest request
    ) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        comment.update(request.content());

        return CommentCreateResponse.from(comment);
    }
}
