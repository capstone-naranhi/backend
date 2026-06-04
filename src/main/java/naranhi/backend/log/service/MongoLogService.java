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
            safetyEventLogRepository.save(
                    SafetyEventLog.builder()
                            .deviceSerialNumber(event.deviceSerialNumber())
                            .eventType(event.eventType())
                            .confidence(event.confidence())
                            .durationSecond(event.durationSecond())
                            .snapshotUrl(event.snapshotUrl())
                            .videoUrl(event.videoUrl())
                            .detectedAt(event.detectedAt())
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.error("MongoDB SafetyEventLog 저장 실패 - deviceSerialNumber: {}, error: {}",
                    event.deviceSerialNumber(), e.getMessage());
        }
    }

    @Async
    public void saveDeviceStatusLog(StatusChangeMessage status, Device device) {
        try {
            ComponentType componentType = ComponentType.valueOf(status.componentType());
            ComponentStatus beforeStatus = ComponentStatus.valueOf(status.beforeStatus());
            ComponentStatus currentStatus = ComponentStatus.valueOf(status.currentStatus());

            deviceStatusChangeLogRepository.save(
                    DeviceStatusLog.builder()
                            .deviceSerialNumber(status.deviceSerialNumber())
                            .deviceId(device.getId())
                            .componentType(componentType)
                            .beforeStatus(beforeStatus)
                            .currentStatus(currentStatus)
                            .reason(status.description())
                            .changedAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.error("MongoDB DeviceStatusLog 저장 실패 - deviceSerialNumber: {}, error: {}",
                    status.deviceSerialNumber(), e.getMessage());
        }
    }
}
