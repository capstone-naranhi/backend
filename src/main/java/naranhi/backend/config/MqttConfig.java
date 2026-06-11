package naranhi.backend.config;

import naranhi.backend.mqtt.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
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
        options.setCleanSession(false);
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
                        "devices/+/events/danger",
                        "devices/+/status/change",
                        "devices/+/heartbeat",
                        "devices/+/signaling/server/+"
                );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(
                1,  // danger        - QoS 1
                1,  // status/change - QoS 1
                0,  // heartbeat     - QoS 0
                1   // signaling     - QoS 1
        );
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // @Bean + @ServiceActivator를 같은 설정 클래스에 선언해야
    // 채널 이름 불일치 없이 안전하게 구독자가 등록됩니다.
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttInboundHandler(MqttSubscriber mqttSubscriber) {
        return mqttSubscriber::handleMessage;
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
        handler.setDefaultQos(0);
        return handler;
    }
}
