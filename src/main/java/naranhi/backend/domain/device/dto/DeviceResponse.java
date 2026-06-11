package naranhi.backend.domain.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.entity.DeviceStatus;
import naranhi.backend.domain.notification.entity.ComponentStatus;
import naranhi.backend.domain.notification.entity.ComponentType;
import naranhi.backend.domain.safety.entity.EventType;
import naranhi.backend.domain.safety.entity.SafetyEvent;
import naranhi.backend.domain.safety.entity.Severity;
import naranhi.backend.log.document.DeviceStatusLog;

public class DeviceResponse {

    private static final int HEARTBEAT_THRESHOLD_SECONDS = 60;

    private static DeviceStatus resolveHeartbeatStatus(LocalDateTime lastHeartbeatAt) {
        if (lastHeartbeatAt == null) {
            return DeviceStatus.OFFLINE;
        }
        return lastHeartbeatAt.isAfter(LocalDateTime.now().minusSeconds(HEARTBEAT_THRESHOLD_SECONDS))
                ? DeviceStatus.ONLINE
                : DeviceStatus.OFFLINE;
    }

    public record DeviceInfo(
            Long deviceId,
            String deviceName,
            String locationName,
            DeviceStatus boardStatus,
            DeviceStatus cameraStatus,
            DeviceStatus micStatus,
            @Schema(description = "마지막 하트비트로부터 60초 이내면 ONLINE, 그렇지 않으면 OFFLINE")
            DeviceStatus heartbeatStatus,
            @Schema(description = "CPU 사용률 (%)")
            @Nullable Double cpuUsage,
            @Schema(description = "메모리 사용률 (%)")
            @Nullable Double memoryUsage
    ) {
        public static DeviceInfo from(Device device) {
            return new DeviceInfo(
                    device.getId(),
                    device.getDeviceName(),
                    device.getLocationName(),
                    device.getBoardStatus(),
                    device.getCameraStatus(),
                    device.getMicStatus(),
                    resolveHeartbeatStatus(device.getLastHeartbeatAt()),
                    device.getCpuUsage(),
                    device.getMemoryUsage()
            );
        }
    }

    public record DeviceList(List<DeviceInfo> devices) {
        public static DeviceList from(List<Device> devices) {
            return new DeviceList(devices.stream().map(DeviceInfo::from).toList());
        }
    }

    public record DeviceDetail(
            Long deviceId,
            String deviceName,
            String locationName,
            String deviceSerialNumber,
            DeviceStatus boardStatus,
            DeviceStatus cameraStatus,
            DeviceStatus micStatus,
            @Schema(description = "마지막 하트비트로부터 60초 이내면 ONLINE, 그렇지 않으면 OFFLINE")
            DeviceStatus heartbeatStatus,
            @Schema(description = "CPU 사용률 (%)")
            @Nullable Double cpuUsage,
            @Schema(description = "메모리 사용률 (%)")
            @Nullable Double memoryUsage,
            @Schema(description = "장치 컴포넌트 상태 변경 로그 (최근 20건, changedAt 내림차순)")
            List<StatusChangeLog> statusChangeLogs
    ) {
        public static DeviceDetail of(Device device, List<DeviceStatusLog> logs) {
            return new DeviceDetail(
                    device.getId(),
                    device.getDeviceName(),
                    device.getLocationName(),
                    device.getDeviceSerialNumber(),
                    device.getBoardStatus(),
                    device.getCameraStatus(),
                    device.getMicStatus(),
                    resolveHeartbeatStatus(device.getLastHeartbeatAt()),
                    device.getCpuUsage(),
                    device.getMemoryUsage(),
                    logs.stream().map(StatusChangeLog::from).toList()
            );
        }
    }

    public record StatusChangeLog(
            ComponentType componentType,
            ComponentStatus beforeStatus,
            ComponentStatus currentStatus,
            LocalDateTime changedAt
    ) {
        public static StatusChangeLog from(DeviceStatusLog log) {
            return new StatusChangeLog(
                    log.getComponentType(),
                    log.getBeforeStatus(),
                    log.getCurrentStatus(),
                    log.getChangedAt()
            );
        }
    }

    public record LiveStreamStatus(
            Long deviceId,
            String deviceName,
            DeviceStatus boardStatus,
            DeviceStatus cameraStatus,
            DeviceStatus micStatus,
            @Schema(description = "실시간 스트리밍 상태, 마지막 하트비트로부터 60초 이내면 ONLINE, 그렇지 않으면 OFFLINE")
            DeviceStatus heartbeatStatus,
            @Schema(description = "CPU 사용률 (%)")
            @Nullable Double cpuUsage,
            @Schema(description = "메모리 사용률 (%)")
            @Nullable Double memoryUsage,
            @Schema(description = "마지막 safety_event의 지속 시간이 0이면 현재 진행 중인 안전 이벤트로 판단해서 전달, 아니면 null")
            @Nullable OngoingSafetyEvent ongoingSafetyEvent
    ) {
        public static LiveStreamStatus of(Device device, SafetyEvent lastSafetyEvent) {
            return new LiveStreamStatus(
                    device.getId(),
                    device.getDeviceName(),
                    device.getBoardStatus(),
                    device.getCameraStatus(),
                    device.getMicStatus(),
                    resolveHeartbeatStatus(device.getLastHeartbeatAt()),
                    device.getCpuUsage(),
                    device.getMemoryUsage(),
                    OngoingSafetyEvent.from(lastSafetyEvent)
            );
        }
    }

    public record RegisteredDevice(
            Long deviceId,
            String deviceName,
            String deviceSerialNumber,
            String locationName
    ) {
        public static RegisteredDevice from(Device device) {
            return new RegisteredDevice(
                    device.getId(),
                    device.getDeviceName(),
                    device.getDeviceSerialNumber(),
                    device.getLocationName()
            );
        }
    }

    public record OngoingSafetyEvent(
            Long safetyEventId,
            EventType eventType,
            Severity severity,
            LocalDateTime detectedAt
    ) {
        public static OngoingSafetyEvent from(SafetyEvent event) {
            if (event == null || event.getDurationSecond() != 0) {
                return null;
            }
            return new OngoingSafetyEvent(
                    event.getId(),
                    event.getEventType(),
                    event.getSeverity(),
                    event.getDetectedAt()
            );
        }
    }
}
