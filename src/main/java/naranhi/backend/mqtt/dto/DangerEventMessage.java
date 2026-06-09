package naranhi.backend.mqtt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

/**
 * Jetson → 서버 토픽: devices/{device_serial}/events/danger
 *
 * <pre>
 * {
 *   "deviceSerial": "JETSON-001",
 *   "eventType":    "FALL" | "CLIMBING" | ...,
 *   "severity":     "DANGER" | "CAUTION" | "INFO",
 *   "confidence":   0.92,
 *   "duration":     5,
 *   "detectedAt":   "2026-06-09T10:00:00+00:00",
 *   "snapshotUrl":  null | "",
 *   "videoUrl":     null | "",
 *   "phase":        "START" | "END"   (선택),
 *   "startedAt":    "..."             (선택),
 *   "endedAt":      "..."             (선택, END phase 시)
 * }
 * </pre>
 *
 * severity는 서버에서 EventType.getDefaultSeverity()로 결정하므로 수신 값은 참고용.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DangerEventMessage(
        String deviceSerial,
        String eventType,
        String severity,
        Double confidence,
        Integer duration,
        OffsetDateTime detectedAt,
        String snapshotUrl,
        String videoUrl,
        String phase,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt
) {
}
