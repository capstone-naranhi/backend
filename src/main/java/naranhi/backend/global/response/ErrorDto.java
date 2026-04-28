package naranhi.backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import naranhi.backend.global.exception.ErrorCode;

public record ErrorDto(
        @NotNull String code,
        @NotNull String message,
        @Nullable @JsonInclude(JsonInclude.Include.NON_NULL) List<String> details
) {
    public static ErrorDto of(ErrorCode errorCode) {
        return new ErrorDto(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ErrorDto of(ErrorCode errorCode, List<String> details) {
        return new ErrorDto(errorCode.getCode(), errorCode.getMessage(), details);
    }
}
