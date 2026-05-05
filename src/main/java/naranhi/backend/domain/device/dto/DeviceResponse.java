package naranhi.backend.domain.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.entity.DeviceStatus;
import naranhi.backend.domain.safety.entity.EventType;
import naranhi.backend.domain.safety.entity.SafetyEvent;
import naranhi.backend.domain.safety.entity.Severity;

public class DeviceResponse {

    private static final int HEARTBEAT_THRESHOLD_SECONDS = 60;

    public record LiveStreamStatus(
            Long deviceId,
            String deviceName,
            DeviceStatus boardStatus,
            DeviceStatus cameraStatus,
            DeviceStatus micStatus,
            @Schema(description = "실시간 스트리밍 상태, 마지막 하트비트로부터 60초 이내면 ONLINE, 그렇지 않으면 OFFLINE")
            DeviceStatus heartbeatStatus,
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
                    OngoingSafetyEvent.from(lastSafetyEvent)
            );
        }

        private static DeviceStatus resolveHeartbeatStatus(LocalDateTime lastHeartbeatAt) {
            if (lastHeartbeatAt == null) {
                return DeviceStatus.OFFLINE;
            }
            return lastHeartbeatAt.isAfter(LocalDateTime.now().minusSeconds(HEARTBEAT_THRESHOLD_SECONDS))
                    ? DeviceStatus.ONLINE
                    : DeviceStatus.OFFLINE;
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
