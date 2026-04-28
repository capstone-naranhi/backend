package naranhi.backend.global.exception.handler;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.global.exception.CustomException;
import naranhi.backend.global.exception.ErrorCode;
import naranhi.backend.global.response.ApiResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ApiResponse.fail(ErrorCode.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ApiResponse.fail(ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    public ApiResponse<?> handleCustomException(CustomException e) {
        return ApiResponse.fail(e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleAllExceptions(Exception e) {
        log.error("handleAllExceptions() throw Exception: {}", e.getMessage());
        e.printStackTrace();
        return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
    }

}
