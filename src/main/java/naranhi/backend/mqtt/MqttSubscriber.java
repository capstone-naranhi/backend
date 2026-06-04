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

        log.debug("MQTT 수신 - topic: {}", topic);

        try {
            // devices/{serial}/events/danger
            if (topic.contains("events/danger")) {
                DangerEventMessage event =
                        objectMapper.readValue(payload, DangerEventMessage.class);
                dangerEventProcessor.process(event);

                // devices/{serial}/status/change
            } else if (topic.contains("status/change")) {
                StatusChangeMessage status =
                        objectMapper.readValue(payload, StatusChangeMessage.class);
                statusChangeProcessor.process(status);

                // devices/{serial}/heartbeat
            } else if (topic.contains("heartbeat")) {
                HeartbeatMessage hb =
                        objectMapper.readValue(payload, HeartbeatMessage.class);
                heartbeatProcessor.process(hb);

                // devices/{serial}/signaling/server/{session_id}
            } else if (topic.contains("signaling/server")) {
                signalingHandler.handle(topic, payload);
            }

        } catch (Exception e) {
            log.error("MQTT 처리 실패 - topic: {}, error: {}", topic, e.getMessage(), e);
        }
    }
}