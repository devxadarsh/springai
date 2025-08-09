package com.example.springai.service.impl;

// src/main/java/com/example/llmcomparator/service/impl/LlmClientServiceImpl.java

import com.example.springai.model.LlmProvider;
import com.example.springai.model.LlmResponse;
import com.example.springai.service.LlmClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

@Service
public class LlmClientServiceImpl implements LlmClientService {

    private static final Logger logger = LoggerFactory.getLogger(LlmClientServiceImpl.class);

    @Value("${llm.openai.api-key:}")
    private String openaiApiKey;

    @Value("${llm.openai.base-url}")
    private String openaiBaseUrl;

    @Value("${llm.openai.model}")
    private String openaiModel;

    @Value("${llm.claude.api-key:}")
    private String claudeApiKey;

    @Value("${llm.claude.base-url}")
    private String claudeBaseUrl;

    @Value("${llm.claude.model}")
    private String claudeModel;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LlmClientServiceImpl(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmResponse queryLlm(String prompt, LlmProvider provider, String sessionId) {
        long startTime = System.currentTimeMillis();

        try {
            switch (provider) {
                case OPENAI:
                    return queryOpenAI(prompt, sessionId, startTime);
                case CLAUDE:
                    return queryClaude(prompt, sessionId, startTime);
                case VERTEX_GEMINI:
                    return queryVertexGemini(prompt, sessionId, startTime);
                default:
                    throw new IllegalArgumentException("Unsupported provider: " + provider);
            }
        } catch (Exception e) {
            logger.error("Error querying {}: {}", provider, e.getMessage());
            long responseTime = System.currentTimeMillis() - startTime;
            return new LlmResponse(prompt, provider, "Error",
                    "Error: " + e.getMessage(), responseTime, 0, sessionId);
        }
    }

    private LlmResponse queryOpenAI(String prompt, String sessionId, long startTime) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", openaiModel,
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    },
                    "max_tokens", 1500,
                    "temperature", 0.7
            );

            String response = webClient.post()
                    .uri(openaiBaseUrl + "/chat/completions")
//                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + "openaiApiKey")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;
            JsonNode jsonResponse = objectMapper.readTree(response);

            String content = jsonResponse.path("choices").get(0).path("message").path("content").asText();
            int totalTokens = jsonResponse.path("usage").path("total_tokens").asInt(0);

            return new LlmResponse(prompt, LlmProvider.OPENAI, openaiModel,
                    content, responseTime, totalTokens, sessionId);

        } catch (WebClientResponseException e) {
            logger.error("OpenAI API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("OpenAI API error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error calling OpenAI: ", e);
            throw new RuntimeException("OpenAI error: " + e.getMessage());
        }
    }

    private LlmResponse queryClaude(String prompt, String sessionId, long startTime) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", claudeModel,
                    "max_tokens", 1500,
                    "temperature", 0.7,
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    }
            );

            String response = webClient.post()
                    .uri(claudeBaseUrl + "/messages")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + claudeApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;
            JsonNode jsonResponse = objectMapper.readTree(response);

            String content = jsonResponse.path("content").get(0).path("text").asText();
            int inputTokens = jsonResponse.path("usage").path("input_tokens").asInt(0);
            int outputTokens = jsonResponse.path("usage").path("output_tokens").asInt(0);

            return new LlmResponse(prompt, LlmProvider.CLAUDE, claudeModel,
                    content, responseTime, inputTokens + outputTokens, sessionId);

        } catch (WebClientResponseException e) {
            logger.error("Claude API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Claude API error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error calling Claude: ", e);
            throw new RuntimeException("Claude error: " + e.getMessage());
        }
    }

    private LlmResponse queryVertexGemini(String prompt, String sessionId, long startTime) {
        // Note: For Vertex AI, you'll need to implement OAuth2 authentication
        // This is a simplified version - in production, use Google Cloud client libraries
        try {
            // This would typically use Google Cloud client libraries for proper authentication
            // For now, returning a placeholder response
            long responseTime = System.currentTimeMillis() - startTime;

            String placeholderResponse = "Vertex Gemini integration requires Google Cloud authentication setup. " +
                    "Please configure proper OAuth2 credentials and use Google Cloud client libraries.";

            return new LlmResponse(prompt, LlmProvider.VERTEX_GEMINI, "gemini-1.5-pro",
                    placeholderResponse, responseTime, 0, sessionId);

        } catch (Exception e) {
            logger.error("Error calling Vertex Gemini: ", e);
            throw new RuntimeException("Vertex Gemini error: " + e.getMessage());
        }
    }

    @Override
    public boolean isProviderAvailable(LlmProvider provider) {
        switch (provider) {
            case OPENAI:
//                return openaiApiKey != null && !openaiApiKey.isEmpty() && !openaiApiKey.startsWith("your-");
                return "openaiApiKey" != null && !"openaiApiKey".isEmpty() && !"openaiApiKey".startsWith("your-");
            case CLAUDE:
                return claudeApiKey != null && !claudeApiKey.isEmpty() && !claudeApiKey.startsWith("your-");
            case VERTEX_GEMINI:
                // Add your Vertex AI availability check here
                return false; // Disabled for now
            default:
                return false;
        }
    }
}
