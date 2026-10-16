package com.moviepick.global.auth;

import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * X-Member-Id 요청 헤더에서 회원 ID를 읽는다.
 */
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String MEMBER_ID_HEADER = "X-Member-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String header = webRequest.getHeader(MEMBER_ID_HEADER);
        if (header == null || header.isBlank()) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}
