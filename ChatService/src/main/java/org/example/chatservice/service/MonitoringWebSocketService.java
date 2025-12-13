package org.example.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.chatservice.config.RabbitConfig;
import org.example.chatservice.dto.ChatMessage;
import org.example.chatservice.dto.ExceededConsumptionDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonitoringWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.CHAT_ALERTS_QUEUE)
    public void consumeMessage(String messageJson) {
        try {
            ExceededConsumptionDTO alert = objectMapper.readValue(messageJson, ExceededConsumptionDTO.class);

            System.out.println("[CHAT] Alertă primită pentru userul: " + alert.getUserId());

            String notificationText = String.format(
                    "ALERTA: Dispozitivul %d a depășit limita! Consum: %.2f (Limita: %.2f)",
                    alert.getDeviceId(),
                    alert.getActualConsumption(),
                    alert.getLimit()
            );

            ChatMessage notification = ChatMessage.builder()
                    .senderId("MONITORING BOT")
                    .recipientId(String.valueOf(alert.getUserId()))
                    .content(notificationText)
                    .isAdmin(false)
                    .build();

            messagingTemplate.convertAndSend("/topic/user/" + alert.getUserId(), notification);

        } catch (Exception e) {
            System.err.println("Eroare la procesarea alertei în ChatService: " + e.getMessage());
            e.printStackTrace();
        }
    }
}