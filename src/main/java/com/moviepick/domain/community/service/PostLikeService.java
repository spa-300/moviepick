package com.moviepick.domain.community.service;

import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostLike;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.repository.PostLikeRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
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
    public void addLike(Long memberId, Long postId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (postLikeRepository.existsByPostIdAndMemberId(postId, memberId)) {
            throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
        }

        PostLike postLike = PostLike.create(post, member);
        postLikeRepository.save(postLike);
    }

    // 게시글 좋아요 취소
    @Transactional
    public void removeLike(Long memberId, Long postId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        PostLike postLike = postLikeRepository
                .findByPostIdAndMemberId(post.getId(), memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("등록된 좋아요가 없습니다."));

        postLikeRepository.delete(postLike);
    }

    // 게시글 좋아요 수 조회
    public long getLikeCount(Long postId) {
        postRepository.findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return postLikeRepository.countByPostId(postId);
    }
}