package com.moviepick.global.auth;

import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberArgumentResolver
        implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        return parameter.hasParameterAnnotation(LoginMember.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {

        // 1. Spring Security에 저장된 인증 정보 가져오기
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // 2. 로그인하지 않은 경우 처리
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long)) {

            // @LoginMember(required = false)인 경우 null 허용
            LoginMember loginMember =
                    parameter.getParameterAnnotation(LoginMember.class);

            if (loginMember != null && !loginMember.required()) {
                return null;
            }

            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }

        // 3. JWT에서 검증된 회원 ID 반환
        return authentication.getPrincipal();
    }
}
