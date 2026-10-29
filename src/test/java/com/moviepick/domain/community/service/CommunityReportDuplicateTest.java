package com.moviepick.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

import com.moviepick.domain.community.entity.Post;
import com.moviepick.domain.community.repository.CommunityReportRepository;
import com.moviepick.domain.community.repository.PostRepository;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import com.moviepick.global.config.JpaAuditingConfig;
import com.moviepick.global.exception.BusinessException;
import com.moviepick.domain.community.exception.ReportErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 사전 중복 확인을 통과한 동시 신고도 DB 유니크 제약으로 409가 되는지 H2로 확인한다. */
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({JpaAuditingConfig.class, CommunityReportService.class})
class CommunityReportDuplicateTest {

    @Autowired
    private CommunityReportService communityReportService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @MockitoSpyBean
    private CommunityReportRepository communityReportRepository;

    @AfterEach
    void tearDown() {
        Mockito.reset(communityReportRepository);
        communityReportRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("같은 회원이 같은 글을 순차로 두 번 신고하면 두 번째는 409(DUPLICATE_REPORT)")
    void sequentialDuplicate() {
        Member writer = saveMember("writer");
        Member reporter = saveMember("reporter");
        Post post = postRepository.save(Post.create(writer, "title", "content"));

        communityReportService.reportPost(reporter.getId(), post.getId(), "SPAM", null);

        assertDuplicate(() -> communityReportService.reportPost(reporter.getId(), post.getId(), "SPAM", null));
        assertThat(communityReportRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("사전 확인을 동시에 통과해도(확인 결과 없음) 유니크 제약으로 409(DUPLICATE_REPORT)이고 저장은 1건")
    void concurrentDuplicate() {
        Member writer = saveMember("writer");
        Member reporter = saveMember("reporter");
        Post post = postRepository.save(Post.create(writer, "title", "content"));

        // 동시 요청: 두 요청 모두 사전 확인에서 "없음"을 본 상황
        doReturn(false).when(communityReportRepository)
                .existsByReporterIdAndTargetTypeAndTargetId(anyLong(), any(), anyLong());

        communityReportService.reportPost(reporter.getId(), post.getId(), "SPAM", null);

        assertDuplicate(() -> communityReportService.reportPost(reporter.getId(), post.getId(), "ABUSE", null));
        assertThat(communityReportRepository.count()).isEqualTo(1);
    }

    private void assertDuplicate(Runnable call) {
        assertThatThrownBy(call::run)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ReportErrorCode.DUPLICATE_REPORT);
    }

    private Member saveMember(String nickname) {
        return memberRepository.save(
                Member.createLocal(nickname + "@test.com", "encoded", nickname));
    }
}
