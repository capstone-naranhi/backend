package naranhi.backend.domain.safety.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    PRONE_SUFFOCATION("자세 뒤집힘 질식 위험", Severity.DANGER),
    BLANKET_SUFFOCATION("이불 질식 위험", Severity.DANGER),
    FALL("낙상", Severity.DANGER),
    CLIMBING("가구 등반", Severity.CAUTION),
    CRYING("울음", Severity.CAUTION),
    EXIT("침대 벗어남", Severity.DANGER);

    private final String description;
    private final Severity defaultSeverity;
}
