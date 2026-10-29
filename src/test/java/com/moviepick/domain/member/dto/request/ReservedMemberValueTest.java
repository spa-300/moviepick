package com.moviepick.domain.member.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * 탈퇴 회원에게 쓰는 값(withdrawn_{번호}, withdrawn_{번호}@withdrawn.local)은 가입·수정에서 거절한다.
 * DB collation(utf8mb4_0900_ai_ci)이 대소문자·악센트를 구분하지 않으므로 그런 변형도 함께 거절한다.
 */
class ReservedMemberValueTest {

    private static final String PASSWORD = "password123!";

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"withdrawn_5", "WithDrawn_5", "WITHDRAWN_", "  withdrawn_5  ", "wíthdrawn_5"})
    @DisplayName("가입: 예약 닉네임은 거절")
    void signupReservedNickname(String nickname) {
        assertThat(validator.validate(new SignupRequest("user@test.com", PASSWORD, nickname))).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a@withdrawn.local", "withdrawn_5@Withdrawn.LOCAL"})
    @DisplayName("가입: 예약 이메일 도메인은 거절")
    void signupReservedEmail(String email) {
        assertThat(validator.validate(new SignupRequest(email, PASSWORD, "보통회원"))).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"withdrawn_5", "WithDrawn_5", "  withdrawn_5"})
    @DisplayName("수정: 예약 닉네임은 거절")
    void updateReservedNickname(String nickname) {
        assertThat(validator.validate(new MemberUpdateRequest(nickname))).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"withdrawn5", "my_withdrawn_", "탈퇴"})
    @DisplayName("예약값이 아닌 닉네임·이메일은 통과")
    void ordinaryValues(String nickname) {
        assertThat(validator.validate(new SignupRequest("withdrawn@test.local", PASSWORD, nickname))).isEmpty();
        assertThat(validator.validate(new MemberUpdateRequest(nickname))).isEmpty();
    }
}
