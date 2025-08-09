package com.example.springai.service;

import com.example.springai.model.LlmProvider;
import com.example.springai.model.LlmResponse;

public interface LlmClientService {

    LlmResponse queryLlm(String prompt, LlmProvider provider, String sessionId);

    boolean isProviderAvailable(LlmProvider provider);
}
