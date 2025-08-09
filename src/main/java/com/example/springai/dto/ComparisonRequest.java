package com.example.springai.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ComparisonRequest {

    @NotBlank(message = "Prompt cannot be blank")
    private String prompt;

    private List<String> providers; // Optional: specific providers to test

    private boolean saveToFile = true;

    public ComparisonRequest() {}

    public ComparisonRequest(String prompt) {
        this.prompt = prompt;
    }

    // Getters and Setters
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public List<String> getProviders() { return providers; }
    public void setProviders(List<String> providers) { this.providers = providers; }

    public boolean isSaveToFile() { return saveToFile; }
    public void setSaveToFile(boolean saveToFile) { this.saveToFile = saveToFile; }
}
