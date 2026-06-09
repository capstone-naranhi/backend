package naranhi.backend.mqtt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

/**
 * Jetson → 서버 토픽: devices/{device_serial}/heartbeat
 *
 * <pre>
 * {
 *   "deviceSerial":   "JETSON-001",
 *   "jetsonStatus":   "ONLINE",
 *   "cameraStatus":   "ONLINE" | "OFFLINE" | "UNKNOWN" | "ERROR",
 *   "micStatus":      "ONLINE" | "OFFLINE" | "ERROR",
 *   "cpuUsage":       12.5,
 *   "memoryUsage":    34.2,
 *   "timestamp":      "2026-06-09T10:00:00+00:00"
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HeartbeatMessage(
        String deviceSerial,
        String jetsonStatus,
        String cameraStatus,
        String micStatus,
        Double cpuUsage,
        Double memoryUsage,
        OffsetDateTime timestamp
) {
}
