package com.moviepick.domain.community.service;

import com.moviepick.domain.community.dto.request.CommentCreateRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 댓글 등록
    @Transactional
    public CommentCreateResponse createComment(
            Long memberId,
            Long postId,
            CommentCreateRequest request
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Post post = postRepository
                .findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = Comment.create(post, member, request.content());

        Comment savedComment = commentRepository.save(comment);

        // 게시글 댓글 수 증가
        post.increaseCommentCount();

        return CommentCreateResponse.from(savedComment);
    }

    // 댓글 목록 조회
    public List<CommentListResponse> getComments(Long postId) {
        postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<Comment> comments =
                commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId);

        return comments.stream()
                .filter(comment -> comment.getStatus() == CommentStatus.VISIBLE)
                .map(CommentListResponse::from)
                .toList();
    }

    // 댓글 삭제 (논리 삭제)
    @Transactional
    public void deleteComment(Long memberId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new IllegalArgumentException("이미 삭제된 댓글입니다.");
        }

        comment.delete();
        comment.getPost().decreaseCommentCount();
    }
}