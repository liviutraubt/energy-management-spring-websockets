package org.example.monitorigservice.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String DEVICE_MEASUREMENTS_QUEUE = "monitoring_queue";

    public static final String DEVICE_SYNC_QUEUE = "monitoring_sync_queue";
    public static final String EXCHANGE_NAME = "sd_sync_exchange";
    public static final String ROUTING_KEY = "device.*";

    @Bean
    public Queue deviceMeasurementsQueue() {
        return new Queue(DEVICE_MEASUREMENTS_QUEUE, false);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public Queue deviceSyncQueue() {
        return new Queue(DEVICE_SYNC_QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue deviceSyncQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deviceSyncQueue).to(exchange).with(ROUTING_KEY);
    }
}
