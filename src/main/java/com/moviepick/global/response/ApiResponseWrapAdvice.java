package com.moviepick.global.response;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 임시 장치: ApiResponse로 감싸지 않고 반환하는 컨트롤러의 응답을 공통 모양으로 맞춘다.
 * - Page → ApiResponse.ok(PageResponse)
 * - 그 외 JSON 본문 → ApiResponse.ok(body)
 * - 본문 없는 204 → 200 + ApiResponse.ok()
 * 각 도메인 컨트롤러가 직접 ApiResponse를 반환하도록 정리되면 제거를 검토한다.
 */
@RestControllerAdvice(basePackages = "com.moviepick")
public class ApiResponseWrapAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        // 문자열 본문은 StringHttpMessageConverter가 쓰므로 감싸면 형 변환 오류가 난다
        return !StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        // 이미 공통 모양이면 그대로
        if (body instanceof ApiResponse<?>) {
            return body;
        }

        // 파일·바이트 응답은 감싸지 않음
        if (body instanceof Resource || body instanceof byte[]) {
            return body;
        }

        int status = currentStatus(response);

        // 본문 없는 성공 응답 (예: 삭제 204) → 200 + 공통 모양
        if (body == null) {
            if (status == HttpStatus.NO_CONTENT.value() || status == HttpStatus.OK.value()) {
                response.setStatusCode(HttpStatus.OK);
                return ApiResponse.ok();
            }
            return null;
        }

        // JSON이 아닌 응답, 성공이 아닌 응답은 감싸지 않음 (성공 모양으로 바꾸면 안 됨)
        if (selectedContentType == null
                || !selectedContentType.isCompatibleWith(MediaType.APPLICATION_JSON)
                || status >= 300) {
            return body;
        }

        Object data = body instanceof Page<?> page
                ? PageResponse.from(page)
                : body;

        return status == HttpStatus.CREATED.value()
                ? ApiResponse.created(data)
                : ApiResponse.ok(data);
    }

    private int currentStatus(ServerHttpResponse response) {
        if (response instanceof ServletServerHttpResponse servletResponse) {
            return servletResponse.getServletResponse().getStatus();
        }
        return HttpStatus.OK.value();
    }
}
