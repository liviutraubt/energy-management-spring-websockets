package org.example.loadbalancingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue mainQueue() {
        return new Queue("monitoring_queue", false);
    }

    @Bean
    public Queue queue0() { return new Queue("monitoring_queue_0", false); }

    @Bean
    public Queue queue1() { return new Queue("monitoring_queue_1", false); }

    @Bean
    public Queue queue2() { return new Queue("monitoring_queue_2", false); }

    @Bean
    public Queue queue3() { return new Queue("monitoring_queue_3", false); }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper());
        converter.setCreateMessageIds(false);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        // FIX: Setăm explicit convertorul JSON pe template
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}