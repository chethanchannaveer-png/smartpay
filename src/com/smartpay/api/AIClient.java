package com.smartpay.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class AIClient {

    private static final String AI_SERVER_URL = System.getenv("AI_SERVER_URL") != null ? 
                                               System.getenv("AI_SERVER_URL") : "http://localhost:5000/match";
    private static final String API_KEY = System.getenv("AI_API_KEY") != null ? 
                                         System.getenv("AI_API_KEY") : "smartpay-secret-token-2026";
    private final HttpClient httpClient;

    public AIClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Sends palm template to Python AI module and returns the response.
     * @param encryptedTemplate Base64 encoded encrypted template
     * @return String JSON response from the server
     */
    public String sendTemplateForMatching(String encryptedTemplate) throws Exception {
        // 1. Prepare JSON payload
        String jsonPayload = "{\"template\": \"" + encryptedTemplate + "\"}";

        // 2. Build Request with API Key
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AI_SERVER_URL))
                .header("Content-Type", "application/json")
                .header("X-API-KEY", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // 4. Send and receive response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("AI Server Error: " + response.statusCode() + " - " + response.body());
        }
    }

    public static void main(String[] args) {
        try {
            AIClient client = new AIClient();
            byte[] dummyTemplate = new byte[256]; // Mock template
            String base64Template = Base64.getEncoder().encodeToString(dummyTemplate);
            String result = client.sendTemplateForMatching(base64Template);
            System.out.println("Result from AI Module: " + result);
        } catch (Exception e) {
            System.err.println("Failed to connect to AI server: " + e.getMessage());
        }
    }
}
