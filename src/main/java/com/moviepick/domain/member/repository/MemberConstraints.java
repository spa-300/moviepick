package com.moviepick.domain.member.repository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

/** members 테이블 유니크 제약 위반 판별. 제약 이름은 db/schema.sql과 Member 엔티티와 같아야 한다. */
public final class MemberConstraints {

    public static final String NICKNAME_UNIQUE = "uk_members_nickname";

    private MemberConstraints() {
    }

    // 닉네임 유니크 제약 위반인지 확인
    public static boolean isDuplicateNickname(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintException) {
                // MySQL은 "members.uk_members_nickname", H2는 "PUBLIC.UK_MEMBERS_NICKNAME INDEX ..."로 알려 준다
                String name = constraintException.getConstraintName();
                return name != null
                        && name.toLowerCase().contains(NICKNAME_UNIQUE);
            }

            cause = cause.getCause();
        }

        return false;
    }
}
