package naranhi.backend.fcm;


import java.util.Map;
import naranhi.backend.domain.notification.entity.NotificationType;

/**
 * FCM 전송 시 사용하는 페이로드 data 필드는 앱에서 딥링크·화면 이동에 활용
 */
public record FcmPayload(
        String title,
        String body,
        Map<String, String> data
) {
    // 안전 이벤트 알림
    public static FcmPayload ofSafety(String eventType, String deviceName, Long notifId) {
        String title = switch (eventType) {
            case "SUFFOCATION_RISK" -> "⚠️ 질식 위험 감지됨";
            case "CRYING" -> "😢 울음 감지됨";
            default -> "⚠️ 위험 감지됨";
        };

        return new FcmPayload(
                title,
                deviceName + " 카메라에서 감지되었습니다.",
                Map.of(
                        "type", NotificationType.SAFETY.name(),
                        "notifId", String.valueOf(notifId),
                        "screen", "NOTIFICATION_DETAIL"
                )
        );
    }

    // 장치 상태 변경 알림
    public static FcmPayload ofDevice(
            String deviceName, String component,
            String prevStatus, String currStatus,
            Long notifId
    ) {
        return new FcmPayload(
                deviceName + " 카메라 " + (currStatus.equals("OFFLINE") ? "연결 끊김" : "연결됨"),
                component + " · " + prevStatus + " → " + currStatus,
                Map.of(
                        "type", NotificationType.DEVICE.name(),
                        "notifId", String.valueOf(notifId),
                        "screen", "DEVICE_DETAIL"
                )
        );
    }
}