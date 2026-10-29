package com.moviepick.domain.member.exception;

import com.moviepick.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    // 회원 조회
    MEMBER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 회원입니다."
    ),

    // 회원가입 - 이메일 중복
    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "이미 사용 중인 이메일입니다."
    ),

    // 회원가입·닉네임 수정 - 닉네임 중복
    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            "이미 사용 중인 닉네임입니다."
    ),

    // 로그인 - 이메일 또는 비밀번호 불일치
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    // 로그인 - 정지된 회원
    MEMBER_SUSPENDED(
            HttpStatus.FORBIDDEN,
            "정지된 회원입니다."
    ),


    MEMBER_WITHDRAWN(
            HttpStatus.FORBIDDEN,
            "탈퇴한 회원입니다."
    ),

    // 정지 상태가 아닌 회원에게 정지 해제를 요청한 경우
    MEMBER_NOT_SUSPENDED(
            HttpStatus.CONFLICT,
            "정지 상태가 아닌 회원입니다."
    );


    private final HttpStatus status;
    private final String message;
}
