package org.example.deviceservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.deviceservice.config.RabbitMQConfig;
import org.example.deviceservice.dto.UserDTO;
import org.example.deviceservice.service.DeviceService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserConsumer {
    private final DeviceService deviceService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(UserDTO userDTO, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        try{
            switch(routingKey){
                case "user.insert":
                    deviceService.insertUser(userDTO);
                    break;

                case "user.delete":
                    deviceService.deleteUser(userDTO.getId());
                    break;

                default:
                    log.warn("Unknown routing key: {}", routingKey);
            }
        }
        catch(Exception e){
            log.error("Error processing message for key {}: {}", routingKey, e.getMessage());
        }
    }
}
