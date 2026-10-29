package com.moviepick.domain.member.dto.request;

import com.moviepick.domain.member.entity.Member;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {

    // 탈퇴 회원용 닉네임(withdrawn_으로 시작)은 쓸 수 없다
    @AssertTrue(message = "사용할 수 없는 닉네임입니다.")
    public boolean isNicknameAllowed() {
        return !Member.isReservedNickname(nickname);
    }
}
