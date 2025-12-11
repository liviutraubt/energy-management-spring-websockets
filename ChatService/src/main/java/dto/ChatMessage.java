package dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private String senderId;
    private String content;
    private String recipientId;
    private boolean isAdmin;
}
