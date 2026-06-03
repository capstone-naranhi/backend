package naranhi.backend.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record DangerEventMessage(
        String serial,
        @JsonProperty("event_type") String eventType,
        String severity,
        double confidence,
        @JsonProperty("duration_seconds") int durationSeconds,
        @JsonProperty("snapshot_url") String snapshotUrl,
        @JsonProperty("video_url") String videoUrl,
        @JsonProperty("detected_at") LocalDateTime detectedAt
) {
}
