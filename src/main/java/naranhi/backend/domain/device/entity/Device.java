package naranhi.backend.domain.device.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import naranhi.backend.global.entity.BaseEntity;

import java.time.LocalDateTime;

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
}
