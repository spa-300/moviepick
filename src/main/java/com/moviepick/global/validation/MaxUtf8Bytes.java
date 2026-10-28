package com.moviepick.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 문자열을 UTF-8로 바꿨을 때 바이트 수가 value 이하인지 검사한다.
 * 비밀번호 암호화(BCrypt)는 72바이트까지만 받으므로 글자 수(@Size)만으로는 부족하다.
 * null은 통과 (@NotBlank 등과 함께 쓴다).
 */
@Documented
@Constraint(validatedBy = MaxUtf8BytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxUtf8Bytes {

    int value();

    String message() default "UTF-8 기준 바이트 수가 너무 큽니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
