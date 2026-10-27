package com.moviepick.global.config;

import com.moviepick.global.exception.BusinessException;
import com.moviepick.global.exception.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 목록 페이지 규칙: page는 0 이상, size는 1~MAX_PAGE_SIZE.
 * 범위를 벗어나면 모든 API에서 같은 400(INVALID_PAGE_REQUEST)으로 응답한다.
 */
@Configuration
public class PageRequestConfig implements WebMvcConfigurer {

    public static final int MAX_PAGE_SIZE = 100;

    // Pageable을 쓰는 API의 최대 크기 (안전장치, 범위 검사는 아래 인터셉터가 먼저 한다)
    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableMaxSizeCustomizer() {
        return resolver -> resolver.setMaxPageSize(MAX_PAGE_SIZE);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PageParameterInterceptor())
                .addPathPatterns("/api/**");
    }

    // 요청 파라미터 page·size 범위 검사
    static class PageParameterInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(
                HttpServletRequest request,
                HttpServletResponse response,
                Object handler
        ) {
            check(request.getParameter("page"), 0, Integer.MAX_VALUE);
            check(request.getParameter("size"), 1, MAX_PAGE_SIZE);
            return true;
        }

        private void check(String value, int min, int max) {
            if (value == null) {
                return;
            }

            int number;
            try {
                number = Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                throw new BusinessException(CommonErrorCode.INVALID_PAGE_REQUEST);
            }

            if (number < min || number > max) {
                throw new BusinessException(CommonErrorCode.INVALID_PAGE_REQUEST);
            }
        }
    }
}
