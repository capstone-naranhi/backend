package naranhi.backend.mqtt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

/**
 * Jetson → 서버 토픽: devices/{device_serial}/status/change
 *
 * <pre>
 * {
 *   "deviceSerial":    "JETSON-001",
 *   "componentType":   "CAMERA" | "MIC" | "BOARD" | "INFERENCE_MODULE",
 *   "previousStatus":  "ONLINE" | "OFFLINE" | "ERROR",
 *   "currentStatus":   "ONLINE" | "OFFLINE" | "ERROR",
 *   "reason":          "heartbeat status change",
 *   "timestamp":       "2026-06-09T10:00:00+00:00"
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusChangeMessage(
        String deviceSerial,
        String componentType,
        String previousStatus,
        String currentStatus,
        String reason,
        OffsetDateTime timestamp
) {
}
