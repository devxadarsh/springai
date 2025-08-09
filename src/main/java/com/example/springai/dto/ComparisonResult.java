package com.example.springai.dto;

// src/main/java/com/example/llmcomparator/dto/ComparisonResult.java


import com.example.springai.model.LlmResponse;

import java.util.List;
import java.util.Map;

public class ComparisonResult {

    private String sessionId;
    private String prompt;
    private List<LlmResponse> responses;
    private Map<String, Object> analysis;
    private String reportFilePath;

    public ComparisonResult() {}

    public ComparisonResult(String sessionId, String prompt, List<LlmResponse> responses) {
        this.sessionId = sessionId;
        this.prompt = prompt;
        this.responses = responses;
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public List<LlmResponse> getResponses() { return responses; }
    public void setResponses(List<LlmResponse> responses) { this.responses = responses; }

    public Map<String, Object> getAnalysis() { return analysis; }
    public void setAnalysis(Map<String, Object> analysis) { this.analysis = analysis; }

    public String getReportFilePath() { return reportFilePath; }
    public void setReportFilePath(String reportFilePath) { this.reportFilePath = reportFilePath; }
}

