package naranhi.backend.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.mqtt.dto.DangerEventMessage;
import naranhi.backend.mqtt.dto.HeartbeatMessage;
import naranhi.backend.mqtt.dto.StatusChangeMessage;
import naranhi.backend.mqtt.SignalingHandler;
import naranhi.backend.mqtt.processor.DangerEventProcessor;
import naranhi.backend.mqtt.processor.HeartbeatProcessor;
import naranhi.backend.mqtt.processor.StatusChangeProcessor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttSubscriber {

    private final DangerEventProcessor dangerEventProcessor;
    private final StatusChangeProcessor statusChangeProcessor;
    private final HeartbeatProcessor heartbeatProcessor;
    private final SignalingHandler signalingHandler;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = (String) message.getPayload();

        log.info("[MQTT] 수신 ← {}", topic);
        log.debug("[MQTT] payload: {}", payload);

        try {
            if (topic.contains("events/danger")) {
                DangerEventMessage event = objectMapper.readValue(payload, DangerEventMessage.class);
                log.info("[MQTT] 위험 이벤트 - serial: {}, type: {}, severity: {}, phase: {}",
                        event.deviceSerial(), event.eventType(), event.severity(), event.phase());
                dangerEventProcessor.process(event);

            } else if (topic.contains("status/change")) {
                StatusChangeMessage status = objectMapper.readValue(payload, StatusChangeMessage.class);
                log.info("[MQTT] 상태 변경 - serial: {}, component: {}, {} → {}",
                        status.deviceSerial(), status.componentType(),
                        status.previousStatus(), status.currentStatus());
                statusChangeProcessor.process(status);

            } else if (topic.contains("heartbeat")) {
                HeartbeatMessage hb = objectMapper.readValue(payload, HeartbeatMessage.class);
                log.debug("[MQTT] 하트비트 - serial: {}, jetson: {}, camera: {}, mic: {}",
                        hb.deviceSerial(), hb.jetsonStatus(), hb.cameraStatus(), hb.micStatus());
                heartbeatProcessor.process(hb);

            } else if (topic.contains("signaling/server")) {
                log.debug("[MQTT] 시그널링 relay ← {}", topic);
                signalingHandler.handle(topic, payload);

            } else {
                log.warn("[MQTT] 처리되지 않은 토픽 - {}", topic);
            }

        } catch (Exception e) {
            log.error("[MQTT] 처리 실패 - topic: {}, error: {}", topic, e.getMessage(), e);
        }
    }
}
