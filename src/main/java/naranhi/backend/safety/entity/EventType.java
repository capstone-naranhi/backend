package naranhi.backend.safety.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    UPSIDE_DOWN_SUFFOCATION("자세 뒤집힘 질식 위험", Severity.DANGER),
    BLANKET_SUFFOCATION("이불 질식 위험", Severity.DANGER),
    FALL("낙상", Severity.DANGER),
    CLIMBING("가구 등반", Severity.DANGER),
    CRYING("울음", Severity.CAUTION),
    WHINING("칭얼거림", Severity.INFO);

    private final String description;
    private final Severity defaultSeverity;
}
