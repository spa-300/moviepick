package com.moviepick.domain.member.dto.request;

import com.moviepick.domain.member.entity.Member;
import com.moviepick.global.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다.")
        @MaxUtf8Bytes(value = 72, message = "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다. (한글은 1자에 3바이트)")
        String password,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname

) {

    // 탈퇴 회원용 이메일(@withdrawn.local)은 쓸 수 없다
    @AssertTrue(message = "사용할 수 없는 이메일입니다.")
    public boolean isEmailAllowed() {
        return !Member.isReservedEmail(email);
    }

    // 탈퇴 회원용 닉네임(withdrawn_으로 시작)은 쓸 수 없다
    @AssertTrue(message = "사용할 수 없는 닉네임입니다.")
    public boolean isNicknameAllowed() {
        return !Member.isReservedNickname(nickname);
    }
}