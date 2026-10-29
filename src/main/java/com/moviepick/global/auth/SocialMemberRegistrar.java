package com.moviepick.global.auth;

import com.moviepick.domain.member.entity.AuthProvider;
import com.moviepick.domain.member.entity.Member;
import com.moviepick.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 소셜 회원을 찾고, 없으면 만든다. 카카오·구글 로그인이 함께 쓴다.
 * 조회와 저장을 각각 새 트랜잭션으로 실행한다. 같은 계정의 첫 로그인이 동시에 들어오면 한쪽 저장이
 * DB 유니크 제약에 걸리는데, 그때 새 트랜잭션에서 다시 조회해야 다른 요청이 커밋한 회원이 보인다
 * (MySQL REPEATABLE READ는 한 트랜잭션 안에서 처음 읽은 시점의 값을 계속 보여 준다).
 */
@Component
public class SocialMemberRegistrar {

    private final MemberRepository memberRepository;
    private final TransactionTemplate newTransaction;

    public SocialMemberRegistrar(
            MemberRepository memberRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.memberRepository = memberRepository;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
    }

    // 소셜 회원 번호를 돌려준다. 없으면 새로 만든다.
    public Long findOrRegister(
            AuthProvider provider,
            String providerId,
            String email,
            String nickname
    ) {
        Optional<Long> found = findMemberId(provider, providerId);

        if (found.isPresent()) {
            return found.get();
        }

        try {
            return newTransaction.execute(status -> memberRepository
                    .saveAndFlush(Member.createSocial(provider, providerId, email, nickname))
                    .getId());
        } catch (DataIntegrityViolationException exception) {
            // 동시 첫 로그인: 다른 요청이 먼저 만든 회원이 있으면 그 회원으로 로그인한다.
            // 다른 이유의 제약 위반이면 그대로 던진다.
            return findMemberId(provider, providerId)
                    .orElseThrow(() -> exception);
        }
    }

    private Optional<Long> findMemberId(AuthProvider provider, String providerId) {
        return newTransaction.execute(status -> memberRepository
                .findByProviderAndProviderId(provider, providerId)
                .map(Member::getId));
    }
}
