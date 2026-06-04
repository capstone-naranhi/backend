package naranhi.backend.domain.device.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import naranhi.backend.domain.notification.entity.ComponentStatus;
import naranhi.backend.domain.notification.entity.ComponentType;
import naranhi.backend.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "device")
@Builder
public class Device extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_name", nullable = false, length = 20)
    private String deviceName;

    @Column(name = "device_serial_number", nullable = false, length = 100, unique = true)
    private String deviceSerialNumber;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_status", nullable = false, length = 10)
    private DeviceStatus boardStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "camera_status", nullable = false, length = 10)
    private DeviceStatus cameraStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "mic_status", nullable = false, length = 10)
    private DeviceStatus micStatus;

    @Column(name = "mqtt_client_id", nullable = false, length = 100, unique = true)
    private String mqttClientId;

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "last_event_at")
    private LocalDateTime lastEventAt;

    // 하트비트 수신 시
    public void updateHeartbeat(LocalDateTime at) {
        this.lastHeartbeatAt = at;
        this.boardStatus = DeviceStatus.ONLINE;
    }

    // 컴포넌트 상태 변경 시
    public void updateComponentStatus(ComponentType componentType, ComponentStatus status) {
        DeviceStatus deviceStatus = status == ComponentStatus.ONLINE
                ? DeviceStatus.ONLINE
                : DeviceStatus.OFFLINE;

        switch (componentType) {
            case CAMERA -> this.cameraStatus = deviceStatus;
            case MIC -> this.micStatus = deviceStatus;
            case BOARD -> this.boardStatus = deviceStatus;
        }
    }

    // 이벤트 수신 시
    public void updateLastEventAt(LocalDateTime at) {
        this.lastEventAt = at;
    }

}
