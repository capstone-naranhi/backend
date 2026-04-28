package naranhi.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAD_REQUEST("COMMON_400", HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요."),
    VALIDATION_FAILED("COMMON_401", HttpStatus.BAD_REQUEST, "요청 데이터가 유효하지 않습니다. 입력값을 확인해주세요."),
    INTERNAL_SERVER_ERROR("COMMON_500", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다. 관리자에게 문의 바랍니다."),
    ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
