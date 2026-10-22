
package com.moviepick.domain.community.service;

import com.moviepick.domain.community.dto.response.PostLikeResponse;
import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostLike;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.repository.PostLikeRepository;
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
public class PostLikeService {

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
            throw new BusinessException(CommonErrorCode.ALREADY_LIKED);
        }

        PostLike postLike = PostLike.create(post, member);
        postLikeRepository.save(postLike);

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
                        new BusinessException(CommonErrorCode.LIKE_NOT_FOUND));

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
                        new BusinessException(CommonErrorCode.NOT_FOUND));
    }
}
