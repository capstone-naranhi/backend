package naranhi.backend.domain.device.document;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import naranhi.backend.domain.notification.entity.ComponentStatus;
import naranhi.backend.domain.notification.entity.ComponentType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "device_status_change_log")
public class DeviceStatusChangeLog {

    @Id
    private String id;

    @Indexed
    @Field("device_id")
    private Long deviceId;

    @Field("component_type")
    private ComponentType componentType;

    @Field("before_status")
    private ComponentStatus beforeStatus;

    @Field("current_status")
    private ComponentStatus currentStatus;

    @Field("reason")
    private String reason;

    @Indexed
    @Field("changed_at")
    private LocalDateTime changedAt;
}
