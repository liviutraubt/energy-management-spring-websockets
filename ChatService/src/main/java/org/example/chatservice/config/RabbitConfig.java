package org.example.chatservice.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String CHAT_ALERTS_QUEUE = "chat_alerts_queue";
    public static final String EXCHANGE_NAME = "sd_sync_exchange";
    public static final String ROUTING_KEY = "notification.exceeded";

    @Bean
    public Queue chatAlertsQueue() {
        return new Queue(CHAT_ALERTS_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue chatAlertsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(chatAlertsQueue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}