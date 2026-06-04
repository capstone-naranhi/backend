package naranhi.backend.mqtt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Jetson → 서버 토픽: devices/{device_serial}/heartbeat
 */
public record HeartbeatMessage(
        @JsonProperty("device_serial_number")
        String deviceSerialNumber,

        @JsonProperty("board_status")
        String boardStatus,

        @JsonProperty("camera_status")
        String cameraStatus,

        @JsonProperty("mic_status")
        String micStatus,

        @JsonProperty("cpu_usage")
        Integer cpuUsage,

        @JsonProperty("memory_usage")
        Integer memoryUsage,

        LocalDateTime timestamp
) {
}
