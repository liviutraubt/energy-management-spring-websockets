package org.example.chatservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model:gemini-1.5-flash}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LlmService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String generateResponse(String userMessage) {
        try {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    model,
                    apiKey
            );

            System.out.println("DEBUG: Calling URL: " + url.replace(apiKey, "***"));
            System.out.println("DEBUG: Model: " + model);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = buildGeminiRequest(userMessage);
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            System.out.println("DEBUG: Request body: " + jsonBody);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("DEBUG: Response received successfully");
                return parseGeminiResponse(response.getBody());
            }

            return "Ne pare rău, serviciul de asistență AI este temporar indisponibil.";

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            System.err.println("Headers: " + e.getResponseHeaders());
            return "Ne pare rău, a apărut o eroare la procesarea mesajului. Te rugăm să contactezi un administrator.";
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            e.printStackTrace();
            return "Ne pare rău, a apărut o eroare la procesarea mesajului. Te rugăm să contactezi un administrator.";
        }
    }

    private Map<String, Object> buildGeminiRequest(String userMessage) {
        String systemPrompt =
                "Ești un asistent virtual pentru un sistem de management energetic (Energy Management System). " +
                        "Ajuți utilizatorii cu întrebări legate de consumul de energie, facturi, dispozitive inteligente și monitorizare. " +
                        "Răspunde în limba română, politicos și concis, cu maximum 3-4 propoziții.";

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", systemPrompt + "\n\nÎntrebare utilizator: " + userMessage);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 500);
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    if (text != null && !text.isEmpty()) {
                        return text.trim();
                    }
                }
            }

            return "Nu am putut genera un răspuns. Te rugăm să contactezi un administrator.";
        } catch (Exception e) {
            System.err.println("Error parsing Gemini response: " + e.getMessage());
            e.printStackTrace();
            return "Eroare la procesarea răspunsului AI.";
        }
    }
}