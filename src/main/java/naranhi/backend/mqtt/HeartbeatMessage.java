package naranhi.backend.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record HeartbeatMessage(
        String serial,
        @JsonProperty("board_status") String boardStatus,
        @JsonProperty("camera_status") String cameraStatus,
        @JsonProperty("mic_status") String micStatus,
        @JsonProperty("cpu_usage") int cpuUsage,
        @JsonProperty("memory_usage") int memoryUsage,
        LocalDateTime timestamp
) {
}