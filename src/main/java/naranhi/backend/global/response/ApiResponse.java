package naranhi.backend.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import java.util.List;
import naranhi.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        @JsonIgnore HttpStatus httpStatus,
        boolean success,
        @Nullable T data,
        @Nullable ErrorDto error
) {
    public static <T> ApiResponse<T> ok(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.OK, true, data, null);
    }

    public static <T> ApiResponse<T> created(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.CREATED, true, data, null);
    }

    public static <T> ApiResponse<T> fail(final ErrorCode c) {
        return new ApiResponse<>(c.getHttpStatus(), false, null, ErrorDto.of(c));
    }

    public static <T> ApiResponse<T> fail(final ErrorCode c, final List<String> details) {
        return new ApiResponse<>(c.getHttpStatus(), false, null, ErrorDto.of(c, details));
    }
}
