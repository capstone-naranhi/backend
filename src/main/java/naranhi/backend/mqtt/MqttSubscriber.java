package naranhi.backend.mqtt;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.domain.notification.entity.NotificationType;
import naranhi.backend.fcm.FcmPayload;
import naranhi.backend.fcm.FcmService;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttSubscriber {

    // 토픽 상수
    private static final String TOPIC_DANGER = "events/danger";
    private static final String TOPIC_STATUS = "status/change";
    private static final String TOPIC_HEARTBEAT = "heartbeat";
    private static final String TOPIC_SIGNALING = "signaling/server";
    private final FcmService fcmService;
    private final SignalingHandler signalingHandler;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = (String) message.getPayload();

        log.debug("MQTT 수신 - topic: {}", topic);

        try {
            if (topic.contains(TOPIC_DANGER)) {
                handleDangerEvent(topic, payload);
            } else if (topic.contains(TOPIC_STATUS)) {
                handleStatusChange(topic, payload);
            } else if (topic.contains(TOPIC_HEARTBEAT)) {
                handleHeartbeat(topic, payload);
            } else if (topic.contains(TOPIC_SIGNALING)) {
                signalingHandler.handle(topic, payload);
            }
        } catch (Exception e) {
            log.error("MQTT 처리 실패 - topic: {}, error: {}", topic, e.getMessage(), e);
        }
    }

    // ─── 위험 감지 이벤트 ─────────────────────────────────────────
    // 토픽: devices/{device_serial}/events/danger

    private void handleDangerEvent(String topic, String payload) throws Exception {
        String serial = extractSerial(topic);
        DangerEventMessage event = objectMapper.readValue(payload, DangerEventMessage.class);

        log.info("위험 감지 - serial: {}, type: {}, severity: {}",
                serial, event.eventType(), event.severity());

        // TODO: serial로 회원 ID 조회 후 알림 INSERT
        // List<Long> memberIds = deviceService.getMemberIdsBySerial(serial);
        // Long notifId = notificationService.createSafetyNotification(event, memberIds);

        // FCM 전송 (임시 notifId)
        Long notifId = 1L;
        List<Long> memberIds = List.of(1L); // TODO: 실제 조회로 교체
        FcmPayload fcmPayload = FcmPayload.ofSafety(
                event.eventType(),
                serial,
                notifId
        );
        fcmService.sendToMembers(memberIds, fcmPayload, NotificationType.SAFETY);
    }

    // ─── 장치 상태 변경 ───────────────────────────────────────────
    // 토픽: devices/{device_serial}/status/change

    private void handleStatusChange(String topic, String payload) throws Exception {
        String serial = extractSerial(topic);
        StatusChangeMessage status = objectMapper.readValue(payload, StatusChangeMessage.class);

        log.info("장치 상태 변경 - serial: {}, {} → {}",
                serial, status.prevStatus(), status.currStatus());

        // TODO: 장치 상태 UPDATE + 알림 INSERT + FCM 전송
    }

    // ─── 하트비트 ─────────────────────────────────────────────────
    // 토픽: devices/{device_serial}/heartbeat

    private void handleHeartbeat(String topic, String payload) throws Exception {
        String serial = extractSerial(topic);
        HeartbeatMessage hb = objectMapper.readValue(payload, HeartbeatMessage.class);

        log.debug("하트비트 - serial: {}, cpu: {}%", serial, hb.cpuUsage());

        // TODO: MySQL 마지막 하트비트 시각 UPDATE
        // TODO: MongoDB 시계열 로그 비동기 저장
    }

    // ─── 유틸 ────────────────────────────────────────────────────

    /**
     * 토픽에서 serial 추출 "devices/{serial}/events/danger" → serial
     */
    private String extractSerial(String topic) {
        // devices / {serial} / ...
        return topic.split("/")[1];
    }
}