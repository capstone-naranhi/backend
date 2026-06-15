package naranhi.backend.domain.safety.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    UPSIDE_DOWN_SUFFOCATION("뒤집힌 자세 질식 위험", Severity.DANGER),
    BLANKET_SUFFOCATION("이불 질식 위험", Severity.DANGER),
    FALL("낙상", Severity.DANGER),
    CLIMBING("가구 등반", Severity.CAUTION),
    CRYING("울음", Severity.CAUTION);

    private final String description;
    private final Severity defaultSeverity;
}
