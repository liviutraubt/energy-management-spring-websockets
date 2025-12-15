package org.example.loadbalancingservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.loadbalancingservice.dto.DeviceDTO;
import org.example.loadbalancingservice.dto.MonitoringDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageRouter {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;


    @RabbitListener(queues = "monitoring_queue")
    public void routeMessage(String messageJson) {
        try {
            MonitoringDTO dto = objectMapper.readValue(messageJson, MonitoringDTO.class);

            DeviceDTO devDTO = dto.getDevice();

            String deviceId = String.valueOf(devDTO.getId());

            int targetQueueIndex = Math.abs(deviceId.hashCode()) % 4;
            String targetQueue = "monitoring_queue_" + targetQueueIndex;

            System.out.println("Routing DeviceID: " + deviceId + " -> " + targetQueue);

            rabbitTemplate.convertAndSend(targetQueue, messageJson);

        } catch (Exception e) {
            System.err.println("Eroare la procesarea mesajului: " + e.getMessage());
            e.printStackTrace();
        }
    }
}