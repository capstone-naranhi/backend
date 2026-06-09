package naranhi.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 인증·인가
    UNAUTHORIZED("AUTH_001", HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN("AUTH_002", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    SESSION_EXPIRED("AUTH_003", HttpStatus.UNAUTHORIZED, "세션이 만료되었습니다. 다시 로그인해주세요."),
    LOGIN_FAILED("AUTH_004", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 회원
    DUPLICATE_EMAIL("MEMBER_001", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND("MEMBER_002", HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

    // 장치
    DEVICE_NOT_FOUND("DEVICE_001", HttpStatus.NOT_FOUND, "존재하지 않는 장치입니다."),
    DEVICE_ALREADY_REGISTERED("DEVICE_002", HttpStatus.CONFLICT, "이미 등록된 장치입니다."),
    DEVICE_ACCESS_DENIED("DEVICE_403", HttpStatus.FORBIDDEN, "해당 디바이스에 접근 권한이 없습니다."),
    DEVICE_PENDING_NOT_FOUND("DEVICE_004", HttpStatus.NOT_FOUND, "장치의 heartbeat가 수신되지 않았거나 만료되었습니다. 장치 전원을 확인해주세요."),

    // 알림
    NOTIFICATION_NOT_FOUND("NOTIF_001", HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),

    // 공통
    BAD_REQUEST("COMMON_400", HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요."),
    VALIDATION_FAILED("COMMON_401", HttpStatus.BAD_REQUEST, "요청 데이터가 유효하지 않습니다. 입력값을 확인해주세요."),
    INTERNAL_SERVER_ERROR("COMMON_500", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다. 관리자에게 문의 바랍니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
