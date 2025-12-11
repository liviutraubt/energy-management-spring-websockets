package controller;


import dto.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import service.BotService;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final BotService botService;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, BotService botService) {
        this.messagingTemplate = messagingTemplate;
        this.botService = botService;
    }

    /*
     * Endpoint pentru mesaje generale (Chat cu Bot sau Broadcast)
     * Clientul trimite la: /app/chat
     */
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage message) {
        // 1. Verificăm dacă e un mesaj pentru BOT
        String botResponse = botService.getBotResponse(message.getContent());

        if (botResponse != null) {
            // Răspundem utilizatorului (sau tuturor, depinde de logică, aici simulăm un reply direct)
            // Pentru simplitate, trimitem pe un topic public, dar filtrat în frontend după ID
            ChatMessage response = ChatMessage.builder()
                    .senderId("BOT")
                    .content(botResponse)
                    .recipientId(message.getSenderId())
                    .build();

            // Trimitem răspunsul către userul specific (folosind convertAndSendToUser e mai sigur)
            messagingTemplate.convertAndSendToUser(
                    message.getSenderId(), "/queue/messages", response);
        } else {
            // TODO: Aici ar veni integrarea cu AI dacă botResponse e null

            // Mesaj default dacă nu există regulă
            ChatMessage response = ChatMessage.builder()
                    .senderId("BOT")
                    .content("Nu am înțeles exact. Te rog reformulează sau contactează un admin.")
                    .recipientId(message.getSenderId())
                    .build();
            messagingTemplate.convertAndSendToUser(
                    message.getSenderId(), "/queue/messages", response);
        }
    }

    /*
     * Endpoint pentru Chat cu Adminul
     * Clientul (User) trimite mesaj care trebuie să ajungă la Admin.
     * Clientul (Admin) trimite mesaj care trebuie să ajungă la User.
     */
    @MessageMapping("/admin-chat")
    public void processAdminMessage(@Payload ChatMessage message) {
        if (message.isAdmin()) {
            // CAZ 1: Adminul răspunde unui User
            // Mesajul se duce direct la userul specificat în recipientId
            messagingTemplate.convertAndSendToUser(
                    message.getRecipientId(), "/queue/messages", message);
        } else {
            // CAZ 2: Un User scrie Adminului
            // Mesajul se duce pe topicul special de admin unde Adminul este abonat
            messagingTemplate.convertAndSend("/topic/admin", message);
        }
    }
}
