package com.example.springai.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "llm_responses")
public class LlmResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LlmProvider provider;

    @Column(nullable = false)
    private String model;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String response;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "token_count")
    private Integer tokenCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "session_id")
    private String sessionId;

    public LlmResponse() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructors
    public LlmResponse(String prompt, LlmProvider provider, String model,
                       String response, Long responseTimeMs, Integer tokenCount, String sessionId) {
        this();
        this.prompt = prompt;
        this.provider = provider;
        this.model = model;
        this.response = response;
        this.responseTimeMs = responseTimeMs;
        this.tokenCount = tokenCount;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public LlmProvider getProvider() { return provider; }
    public void setProvider(LlmProvider provider) { this.provider = provider; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
