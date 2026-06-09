package naranhi.backend.log.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.notification.entity.ComponentStatus;
import naranhi.backend.domain.notification.entity.ComponentType;
import naranhi.backend.log.document.DeviceStatusLog;
import naranhi.backend.log.document.SafetyEventLog;
import naranhi.backend.log.repository.SafetyEventLogRepository;
import naranhi.backend.domain.device.repository.mongo.DeviceStatusChangeLogRepository;
import naranhi.backend.mqtt.dto.DangerEventMessage;
import naranhi.backend.mqtt.dto.StatusChangeMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MongoLogService {

    private final SafetyEventLogRepository safetyEventLogRepository;
    private final DeviceStatusChangeLogRepository deviceStatusChangeLogRepository;

    @Async
    public void saveDangerEventLog(DangerEventMessage event) {
        try {
            LocalDateTime detectedAt = event.detectedAt() != null
                    ? event.detectedAt().toLocalDateTime()
                    : LocalDateTime.now();

            safetyEventLogRepository.save(
                    SafetyEventLog.builder()
                            .deviceSerialNumber(event.deviceSerial())
                            .eventType(event.eventType())
                            .confidence(event.confidence())
                            .durationSecond(event.duration())
                            .snapshotUrl(event.snapshotUrl())
                            .videoUrl(event.videoUrl())
                            .detectedAt(detectedAt)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.error("MongoDB SafetyEventLog 저장 실패 - serial: {}, error: {}",
                    event.deviceSerial(), e.getMessage());
        }
    }

    @Async
    public void saveDeviceStatusLog(StatusChangeMessage status, Device device) {
        try {
            ComponentType componentType = ComponentType.valueOf(status.componentType());
            ComponentStatus previousStatus = ComponentStatus.valueOf(status.previousStatus());
            ComponentStatus currentStatus = ComponentStatus.valueOf(status.currentStatus());

            deviceStatusChangeLogRepository.save(
                    DeviceStatusLog.builder()
                            .deviceSerialNumber(status.deviceSerial())
                            .deviceId(device.getId())
                            .componentType(componentType)
                            .beforeStatus(previousStatus)
                            .currentStatus(currentStatus)
                            .reason(status.reason())
                            .changedAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.error("MongoDB DeviceStatusLog 저장 실패 - serial: {}, error: {}",
                    status.deviceSerial(), e.getMessage());
        }
    }
}
