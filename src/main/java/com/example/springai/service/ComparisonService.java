package com.example.springai.service;

import com.example.springai.dto.ComparisonRequest;
import com.example.springai.dto.ComparisonResult;
import com.example.springai.model.LlmResponse;

import java.util.List;

public interface ComparisonService {
    ComparisonResult compareModels(ComparisonRequest request);
    List<LlmResponse> getComparisonHistory(String sessionId);
    ComparisonResult analyzeResponses(List<LlmResponse> responses);
}
