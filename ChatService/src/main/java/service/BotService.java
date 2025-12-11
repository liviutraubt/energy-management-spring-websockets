package service;

import org.springframework.stereotype.Service;

@Service
public class BotService {

    public String getBotResponse(String userMessage) {
        String msg = userMessage.toLowerCase();

        if (msg.contains("salut") || msg.contains("buna")) {
            return "Salut! Cu ce te pot ajuta astazi in legatura cu consumul de energie?";
        }
        if (msg.contains("consum") && msg.contains("mare")) {
            return "Dacă ai un consum mare, verifică electrocasnicele mari sau senzorii de temperatură.";
        }
        if (msg.contains("factura")) {
            return "Facturile se emit automat la sfârșitul lunii. Le poți vedea în dashboard.";
        }
        if (msg.contains("ore") || msg.contains("program")) {
            return "Dispozitivele inteligente pot fi programate din secțiunea 'Device Management'.";
        }
        if (msg.contains("contact") || msg.contains("admin")) {
            return "Poți contacta un administrator folosind butonul 'Chat cu Admin'.";
        }

        return null;
    }
}