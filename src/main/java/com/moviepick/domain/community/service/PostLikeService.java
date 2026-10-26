
package com.moviepick.domain.community.service;

import com.moviepick.domain.community.dto.response.PostLikeResponse;
import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostLike;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.exception.PostErrorCode;
import com.moviepick.domain.community.repository.PostLikeRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private static final String POST_LIKE_UNIQUE_CONSTRAINT =
            "uk_post_like_member";

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 게시글 좋아요 등록
    @Transactional
    public PostLikeResponse addLike(Long memberId, Long postId) {
        Post post = findVisiblePost(postId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        if (postLikeRepository.existsByPostIdAndMemberId(postId, memberId)) {
            throw new BusinessException(PostErrorCode.POST_ALREADY_LIKED);
        }

        PostLike postLike = PostLike.create(post, member);

        try {
            // 유니크 제약 위반을 이 메서드 안에서 확인한다.
            postLikeRepository.saveAndFlush(postLike);
        } catch (DataIntegrityViolationException exception) {
            if (isPostLikeUniqueConstraintViolation(exception)) {
                throw new BusinessException(PostErrorCode.POST_ALREADY_LIKED);
            }

            throw exception;
        }

        long likeCount = postLikeRepository.countByPostId(postId);

        return new PostLikeResponse(true, likeCount);
    }

    // 게시글 좋아요 취소
    @Transactional
    public PostLikeResponse removeLike(Long memberId, Long postId) {
        Post post = findVisiblePost(postId);

        PostLike postLike = postLikeRepository
                .findByPostIdAndMemberId(post.getId(), memberId)
                .orElseThrow(() ->
                        new BusinessException(
                                PostErrorCode.POST_LIKE_NOT_FOUND));

        postLikeRepository.delete(postLike);

        long likeCount = postLikeRepository.countByPostId(postId);

        return new PostLikeResponse(false, likeCount);
    }

    // 게시글 좋아요 수 조회
    public long getLikeCount(Long postId) {
        findVisiblePost(postId);
        return postLikeRepository.countByPostId(postId);
    }

    // 특정 회원이 게시글에 좋아요를 눌렀는지 확인
    public boolean isLikedByMember(Long postId, Long memberId) {
        return postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
    }

    // 공개 상태인 게시글 조회
    private Post findVisiblePost(Long postId) {
        return postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(PostErrorCode.POST_NOT_FOUND));
    }

    // 게시글 좋아요 유니크 제약 위반인지 확인
    private boolean isPostLikeUniqueConstraintViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintException) {
                return POST_LIKE_UNIQUE_CONSTRAINT.equals(
                        constraintException.getConstraintName());
            }

            cause = cause.getCause();
        }

        return false;
    }
}
