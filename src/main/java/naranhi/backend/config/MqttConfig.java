package naranhi.backend.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@IntegrationComponentScan
public class MqttConfig {

    @Value("${spring.mqtt.broker-url}")
    private String brokerUrl;

    @Value("${spring.mqtt.client-id}")
    private String clientId;

    @Value("${spring.mqtt.username}")
    private String username;

    @Value("${spring.mqtt.password}")
    private String password;

    // ─── 공통 팩토리 ─────────────────────────────────────────────

    @Bean
    public DefaultMqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);      // 연결 끊긴 사이 메시지 유실 방지
        options.setKeepAliveInterval(60);
        factory.setConnectionOptions(options);
        return factory;
    }

    // ─── 수신 채널 ────────────────────────────────────────────────

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId + "-sub",
                        mqttClientFactory(),
                        // 구독할 토픽 목록
                        "devices/+/events/danger",    // 위험 감지 이벤트
                        "devices/+/status/change",    // 장치 상태 변경
                        "devices/+/heartbeat",        // 하트비트
                        "devices/+/signaling/server/+" // WebRTC signaling (보드 → 서버)
                );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(
                2,  // danger       - 정확히 1번
                1,  // status/change
                1,  // heartbeat
                0   // signaling    - 실시간성 중요, 재전송 불필요
        );
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // ─── 발신 채널 (서버 → 보드 signaling 용) ────────────────────

    @Bean
    public MessageChannel mqttOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageHandler mqttOutboundHandler() {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                clientId + "-pub",
                mqttClientFactory()
        );
        handler.setAsync(true);
        handler.setDefaultQos(0); // signaling은 QoS 0
        return handler;
    }
}