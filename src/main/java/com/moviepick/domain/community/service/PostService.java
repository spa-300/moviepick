
package com.moviepick.domain.community.service;

import com.moviepick.domain.community.dto.request.PostCreateRequest;
import com.moviepick.domain.community.dto.request.PostUpdateRequest;
import com.moviepick.domain.community.dto.response.PostCreateResponse;
import com.moviepick.domain.community.dto.response.PostDetailResponse;
import com.moviepick.domain.community.dto.response.PostListResponse;
import com.moviepick.domain.community.dto.response.PostUpdateResponse;
import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.entity.PostStatus;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostLikeService postLikeService;

    // 게시글 작성
    @Transactional
    public PostCreateResponse create(Long memberId, PostCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        Post post = Post.create(
                member,
                request.title(),
                request.content()
        );

        Post savedPost = postRepository.save(post);

        return PostCreateResponse.from(savedPost);
    }

    // 게시글 목록 조회
    public Page<PostListResponse> getPosts(
            String q,
            String sort,
            Pageable pageable
    ) {
        String sortType = (sort == null || sort.isBlank())
                ? "latest"
                : sort;

        boolean hasKeyword = q != null && !q.isBlank();
        String keyword = hasKeyword ? q.trim() : null;

        Page<Post> posts;

        if (hasKeyword && "popular".equalsIgnoreCase(sortType)) {
            // 검색 결과 인기순
            posts = postRepository.searchPopularPosts(
                    PostStatus.VISIBLE,
                    keyword,
                    PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize()
                    )
            );
        } else if ("popular".equalsIgnoreCase(sortType)) {
            // 전체 게시글 인기순
            posts = postRepository.findPopularPosts(
                    PostStatus.VISIBLE,
                    PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize()
                    )
            );
        } else {
            // 최신순: 게시글과 좋아요 수를 함께 조회
            Page<Object[]> results;

            if (hasKeyword) {
                results = postRepository.searchWithLikeCount(
                        PostStatus.VISIBLE,
                        keyword,
                        PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                );
            } else {
                results = postRepository.findAllWithLikeCountByStatus(
                        PostStatus.VISIBLE,
                        PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                );
            }

            return results.map(row -> {
                Post post = (Post) row[0];
                long likeCount = ((Number) row[1]).longValue();

                return PostListResponse.from(post, likeCount);
            });
        }

        // 인기순 조회는 기존 정렬 로직 유지
        return posts.map(post ->
                PostListResponse.from(
                        post,
                        postLikeService.getLikeCount(post.getId())
                )
        );
    }

    // 게시글 상세 조회
    @Transactional
    public PostDetailResponse getPost(Long memberId, Long postId) {
        Post post = postRepository
                .findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        // 조회수 증가
        post.increaseViewCount();

        // 좋아요 수 조회
        long likeCount = postLikeService.getLikeCount(postId);

        // 비로그인 사용자는 좋아요 여부를 false로 처리
        boolean likedByMe = memberId != null
                && postLikeService.isLikedByMember(postId, memberId);

        return PostDetailResponse.from(post, likeCount, likedByMe);
    }

    // 게시글 수정
    @Transactional
    public PostUpdateResponse updatePost(
            Long memberId,
            Long postId,
            PostUpdateRequest request
    ) {
        Post post = postRepository
                .findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        // 작성자 본인인지 확인
        if (!post.getMember().getId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

        // 제목과 내용 중 최소 하나는 입력해야 함
        if (request.title() == null && request.content() == null) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }

        // 전달된 필드만 수정하고 생략한 필드는 기존 값 유지
        String title = request.title() != null
                ? request.title()
                : post.getTitle();

        String content = request.content() != null
                ? request.content()
                : post.getContent();

        post.update(title, content);

        return PostUpdateResponse.from(post);
    }

    // 게시글 삭제 (논리 삭제)
    @Transactional
    public void deletePost(Long memberId, Long postId) {
        Post post = postRepository
                .findByIdAndStatus(postId, PostStatus.VISIBLE)
                .orElseThrow(() ->
                        new BusinessException(CommonErrorCode.NOT_FOUND));

        // 작성자 본인인지 확인
        if (!post.getMember().getId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

        post.delete();
    }
}
