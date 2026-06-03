package naranhi.backend.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * WebRTC Signaling 중계 보드 → 서버 수신 → 앱 방향으로 relay
 * <p>
 * 수신 토픽: devices/{serial}/signaling/server/{session_id} 발신 토픽: devices/{serial}/signaling/client/{session_id}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SignalingHandler {

    private final MessageChannel mqttOutputChannel;

    public void handle(String topic, String payload) {
        // devices/{serial}/signaling/server/{session_id}
        //       → devices/{serial}/signaling/client/{session_id}
        String clientTopic = topic.replace(
                "signaling/server",
                "signaling/client"
        );

        log.debug("Signaling relay: {} → {}", topic, clientTopic);

        // 앱 방향으로 relay
        mqttOutputChannel.send(
                MessageBuilder.withPayload(payload)
                        .setHeader(MqttHeaders.TOPIC, clientTopic)
                        .setHeader(MqttHeaders.QOS, 0)
                        .build()
        );
    }
}