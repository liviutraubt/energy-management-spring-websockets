package org.example.loadbalancingservice.service;

import lombok.RequiredArgsConstructor;
import org.example.loadbalancingservice.dto.MonitoringDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageRouter {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "monitoring_queue")
    public void routeMessage(MonitoringDTO dto) {
        try {
            Double deviceId = Double.valueOf(dto.getDevice().getId());

            int targetQueueIndex = (int) (deviceId % 4);
            String targetQueue = "monitoring_queue_" + targetQueueIndex;

            System.out.println("Routing DeviceID: " + deviceId + " -> " + targetQueue);

            rabbitTemplate.convertAndSend(targetQueue, dto);

        } catch (Exception e) {
            System.err.println("Eroare la procesarea mesajului: " + e.getMessage());

        }
    }
}