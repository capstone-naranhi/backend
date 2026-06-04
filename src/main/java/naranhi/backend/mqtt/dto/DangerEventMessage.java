package naranhi.backend.mqtt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Jetson → 서버 토픽: devices/{device_serial}/events/danger
 * <p>
 * severity는 EventType.getDefaultSeverity()로 서버에서 결정 MQTT 메시지에 severity 포함 필요 없음
 */
public record DangerEventMessage(
        @JsonProperty("device_serial_number")
        String deviceSerialNumber,

        @JsonProperty("event_type")
        String eventType,          // EventType enum name (ex. "CRYING", "FALL")

        Double confidence,

        @JsonProperty("duration_second")
        Integer durationSecond,

        @JsonProperty("snapshot_url")
        String snapshotUrl,

        @JsonProperty("video_url")
        String videoUrl,

        @JsonProperty("detected_at")
        LocalDateTime detectedAt
) {
}
