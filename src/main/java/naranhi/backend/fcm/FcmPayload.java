package naranhi.backend.fcm;


import java.util.Map;
import naranhi.backend.domain.notification.entity.NotificationType;
import naranhi.backend.domain.safety.entity.EventType;

/**
 * FCM 전송 시 사용하는 페이로드 data 필드는 앱에서 딥링크·화면 이동에 활용
 */

public record FcmPayload(
        String title,
        String body,
        Map<String, String> data
) {
    /**
     * 안전 이벤트 알림 EventType.description 을 제목으로 사용
     */
    public static FcmPayload ofSafety(
            EventType eventType,
            String deviceName,
            Long notifId
    ) {

        // Severity에 따라 이모지 결정
        String emoji = switch (eventType.getDefaultSeverity()) {
            case DANGER -> "🚨";
            case CAUTION -> "⚠️";
            case INFO -> "ℹ️";
        };

        return new FcmPayload(
                emoji + " " + eventType.getDescription(),
                deviceName + " 카메라에서 감지되었습니다.",
                Map.of(
                        "type", NotificationType.SAFETY.name(),
                        "notifId", String.valueOf(notifId),
                        "screen", "NOTIFICATION_DETAIL"
                )
        );
    }

    /**
     * 장치 상태 변경 알림
     */
    public static FcmPayload ofDevice(
            String deviceName,
            String componentType,
            String beforeStatus,
            String currentStatus,
            Long notifId
    ) {

        boolean isOffline = "OFFLINE".equals(currentStatus) || "ERROR".equals(currentStatus);

        return new FcmPayload(
                deviceName + " " + (isOffline ? "연결 끊김" : "연결됨"),
                componentType + " · " + beforeStatus + " → " + currentStatus,
                Map.of(
                        "type", NotificationType.DEVICE.name(),
                        "notifId", String.valueOf(notifId),
                        "screen", "DEVICE_DETAIL"
                )
        );
    }
}