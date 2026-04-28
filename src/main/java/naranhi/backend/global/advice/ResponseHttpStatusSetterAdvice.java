package naranhi.backend.global.advice;

import naranhi.backend.global.response.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * ApiResponse 객체를 반환하는 모든 컨트롤러 메서드에 대해 HTTP 상태 코드를 설정하는 ResponseBodyAdvice 구현체입니다.
 */
@RestControllerAdvice
public class ResponseHttpStatusSetterAdvice implements ResponseBodyAdvice<ApiResponse<?>> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().equals(ApiResponse.class);
    }

    @Override
    public ApiResponse<?> beforeBodyWrite(
            ApiResponse<?> body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        HttpStatus status = body.httpStatus();
        response.setStatusCode(status);
        return body;
    }
}