package naranhi.backend.log.document;


import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Builder
@Document(collection = "danger_event_log")
public class SafetyEventLog {
    @Id
    private String id;

    @Field("device_serial_number")
    private String deviceSerialNumber;

    @Field("event_type")
    private String eventType;

    @Field("severity")
    private String severity;

    @Field("confidence")
    private Double confidence;

    @Field("duration_second")
    private Integer durationSecond;

    @Field("snapshot_url")
    private String snapshotUrl;

    @Field("video_url")
    private String videoUrl;

    @Field("detected_at")
    @Indexed
    private LocalDateTime detectedAt;

    @Field("created_at")
    private LocalDateTime createdAt;
}
