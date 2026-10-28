package com.moviepick.domain.community.service;

import com.moviepick.domain.community.dto.request.CommentCreateRequest;
import com.moviepick.domain.community.dto.request.CommentUpdateRequest;
import com.moviepick.domain.community.dto.response.CommentCreateResponse;
import com.moviepick.domain.community.dto.response.CommentListResponse;
import com.moviepick.domain.community.entity.Comment;
import com.moviepick.domain.community.entity.CommentStatus;
import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.exception.CommentErrorCode;
import com.moviepick.domain.community.exception.PostErrorCode;
import com.moviepick.domain.community.repository.CommentRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.exception.MemberErrorCode;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;
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
                        new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository
                .findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(PostErrorCode.POST_NOT_FOUND));

        // 댓글 수 원자적 증가를 먼저 실행해 게시글 줄 쓰기 잠금을 먼저 잡는다 (insert가 실패하면 함께 롤백)
        postRepository.increaseCommentCount(postId);

        Comment comment = Comment.create(post, member, request.content());
        Comment savedComment = commentRepository.save(comment);

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
                        new BusinessException(PostErrorCode.POST_NOT_FOUND));

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
                        new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }

        // 게시글이 공개 상태일 때만 댓글 삭제 허용
        postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(CommentErrorCode.COMMENT_ACCESS_DENIED);
        }

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }

        comment.delete();

        // 댓글 수 원자적 감소
        postRepository.decreaseCommentCount(postId);
    }

    // 댓글 삭제 (댓글 ID 기준)
    @Transactional
    public void deleteCommentById(Long memberId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        Long postId = comment.getPost().getId();

        // 게시글이 공개 상태일 때만 댓글 삭제 허용
        postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(CommentErrorCode.COMMENT_ACCESS_DENIED);
        }

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }

        comment.delete();

        // 댓글 수 원자적 감소
        postRepository.decreaseCommentCount(postId);
    }

    // 댓글 수정
    @Transactional
    public CommentCreateResponse updateComment(
            Long memberId,
            Long commentId,
            CommentUpdateRequest request
    ) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 게시글 번호는 댓글에서 꺼낸다
        Long postId = comment.getPost().getId();

        // 게시글이 공개 상태일 때만 댓글 수정 허용
        postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(CommentErrorCode.COMMENT_ACCESS_DENIED);
        }

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }

        comment.update(request.content());

        return CommentCreateResponse.from(comment);
    }
}