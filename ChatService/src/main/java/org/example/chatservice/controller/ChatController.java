package org.example.chatservice.controller;

import org.example.chatservice.dto.ChatMessage;
import org.example.chatservice.service.BotService;
import org.example.chatservice.service.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final BotService botService;
    private final LlmService llmService;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, BotService botService, LlmService llmService) {
        this.messagingTemplate = messagingTemplate;
        this.botService = botService;
        this.llmService = llmService;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage message) {
        String botResponseContent = botService.getBotResponse(message.getContent());

        if (botResponseContent == null) {
            botResponseContent = llmService.generateResponse(message.getContent());
        }

        ChatMessage response = ChatMessage.builder()
                .senderId("BOT")
                .content(botResponseContent)
                .recipientId(message.getSenderId())
                .isAdmin(false)
                .build();

        messagingTemplate.convertAndSend("/topic/user/" + message.getSenderId(), response);
    }

    @MessageMapping("/admin-chat")
    public void processAdminMessage(@Payload ChatMessage message) {
        if (message.isAdmin()) {
            messagingTemplate.convertAndSend("/topic/user/" + message.getRecipientId(), message);

        } else {
            messagingTemplate.convertAndSend("/topic/admin", message);
        }
    }
}