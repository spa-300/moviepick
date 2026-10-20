package com.moviepick.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 로그인 회원 ID(Long)를 주입한다.
 * 사용 예: createReview(@LoginMember Long memberId, ...)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMember {
    boolean required() default true;   // false면 비로그인 허용 (memberId가 null로 주입됨)
}
