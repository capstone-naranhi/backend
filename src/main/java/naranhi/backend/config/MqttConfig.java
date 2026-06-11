package naranhi.backend.config;

import naranhi.backend.mqtt.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
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

    // ─── 수신: 어댑터 → MqttSubscriber (Integration Flow로 명시적 배선) ──

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId + "-sub",
                        mqttClientFactory(),
                        "devices/+/events/danger",    // 위험 감지 이벤트
                        "devices/+/status/change",    // 장치 상태 변경
                        "devices/+/heartbeat",        // 하트비트
                        "devices/+/signaling/server/+" // WebRTC signaling (보드 → 서버)
                );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(
                1,  // danger        - QoS 1
                1,  // status/change - QoS 1
                0,  // heartbeat     - QoS 0
                1   // signaling     - QoS 1
        );
        // 출력 채널은 mqttInboundFlow에서 설정하므로 여기서 지정하지 않음
        return adapter;
    }

    @Bean
    public IntegrationFlow mqttInboundFlow(MqttSubscriber mqttSubscriber) {
        return IntegrationFlow
                .from(mqttInboundAdapter())
                .handle(mqttSubscriber, "handleMessage")
                .get();
    }

    // ─── 발신 채널 (서버 → 보드 signaling 용) ────────────────────

    @Bean
    public MessageChannel mqttOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
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
