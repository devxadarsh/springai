package com.example.springai.service.impl;

import com.example.springai.dto.ComparisonRequest;
import com.example.springai.dto.ComparisonResult;
import com.example.springai.model.LlmProvider;
import com.example.springai.model.LlmResponse;
import com.example.springai.repository.LlmResponseRepository;
import com.example.springai.service.ComparisonService;
import com.example.springai.service.LlmClientService;
import com.example.springai.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ComparisonServiceImpl implements ComparisonService {

    private static final Logger logger = LoggerFactory.getLogger(ComparisonServiceImpl.class);

    private final LlmClientService llmClientService;
    private final LlmResponseRepository responseRepository;
    private final ReportService reportService;

    @Autowired
    public ComparisonServiceImpl(LlmClientService llmClientService, LlmResponseRepository responseRepository,
                                 ReportService reportService) {
        this.llmClientService = llmClientService;
        this.responseRepository = responseRepository;
        this.reportService = reportService;
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    public ComparisonResult compareModels(ComparisonRequest request) {
        String sessionId = UUID.randomUUID().toString();
        logger.info("Starting comparison with session ID: {}", sessionId);

        // Determine which providers to query
        List<LlmProvider> providersToQuery = determineProviders(request);

        // Query all providers concurrently
        List<CompletableFuture<LlmResponse>> futures = providersToQuery.stream()
                .map(provider -> CompletableFuture.supplyAsync(
                        () -> llmClientService.queryLlm(request.getPrompt(), provider, sessionId),
                        executorService))
                .toList();

        // Wait for all responses
        List<LlmResponse> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // Save responses to database
        responses.forEach(responseRepository::save);

        // Create comparison result
//        ComparisonResult result = new ComparisonResult(sessionId, request.getPrompt(), responses);
        ComparisonResult result = new ComparisonResult();

        // Analyze responses
        result = analyzeResponses(responses);
        result.setSessionId(sessionId);
        result.setPrompt(request.getPrompt());

        // Generate report file if requested
        if (request.isSaveToFile()) {
            try {
                String reportPath = reportService.generateMarkdownReport(result);
                result.setReportFilePath(reportPath);
                logger.info("Report saved to: {}", reportPath);
            } catch (Exception e) {
                logger.error("Error generating report: ", e);
            }
        }

        return result;
    }

    private List<LlmProvider> determineProviders(ComparisonRequest request) {
        if (request.getProviders() != null && !request.getProviders().isEmpty()) {
            return request.getProviders().stream()
                    .map(LlmProvider::valueOf)
                    .filter(llmClientService::isProviderAvailable)
                    .collect(Collectors.toList());
        } else {
            // Query all available providers
            return Arrays.stream(LlmProvider.values())
                    .filter(llmClientService::isProviderAvailable)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<LlmResponse> getComparisonHistory(String sessionId) {
        return responseRepository.findBySessionIdOrderByCreatedAt(sessionId);
    }

    @Override
    public ComparisonResult analyzeResponses(List<LlmResponse> responses) {
        if (responses.isEmpty()) {
            return new ComparisonResult();
        }

        Map<String, Object> analysis = new HashMap<>();

        // Response time analysis
        OptionalDouble avgResponseTime = responses.stream()
                .mapToLong(LlmResponse::getResponseTimeMs)
                .average();

        LlmResponse fastest = responses.stream()
                .min(Comparator.comparing(LlmResponse::getResponseTimeMs))
                .orElse(null);

        LlmResponse slowest = responses.stream()
                .max(Comparator.comparing(LlmResponse::getResponseTimeMs))
                .orElse(null);

        // Response length analysis
        OptionalDouble avgResponseLength = responses.stream()
                .mapToInt(r -> r.getResponse().length())
                .average();

        LlmResponse shortest = responses.stream()
                .min(Comparator.comparing(r -> r.getResponse().length()))
                .orElse(null);

        LlmResponse longest = responses.stream()
                .max(Comparator.comparing(r -> r.getResponse().length()))
                .orElse(null);

        // Token usage analysis
        OptionalDouble avgTokens = responses.stream()
                .filter(r -> r.getTokenCount() != null && r.getTokenCount() > 0)
                .mapToInt(LlmResponse::getTokenCount)
                .average();

        // Build analysis map
        analysis.put("totalResponses", responses.size());
        analysis.put("averageResponseTime", avgResponseTime.orElse(0.0));
        analysis.put("fastestProvider", fastest != null ? fastest.getProvider().getDisplayName() : "N/A");
        analysis.put("fastestTime", fastest != null ? fastest.getResponseTimeMs() : 0);
        analysis.put("slowestProvider", slowest != null ? slowest.getProvider().getDisplayName() : "N/A");
        analysis.put("slowestTime", slowest != null ? slowest.getResponseTimeMs() : 0);

        analysis.put("averageResponseLength", avgResponseLength.orElse(0.0));
        analysis.put("shortestProvider", shortest != null ? shortest.getProvider().getDisplayName() : "N/A");
        analysis.put("shortestLength", shortest != null ? shortest.getResponse().length() : 0);
        analysis.put("longestProvider", longest != null ? longest.getProvider().getDisplayName() : "N/A");
        analysis.put("longestLength", longest != null ? longest.getResponse().length() : 0);

        analysis.put("averageTokens", avgTokens.orElse(0.0));

        // Content analysis
        Map<String, Integer> commonWords = analyzeCommonWords(responses);
        analysis.put("commonWords", commonWords);

        ComparisonResult result = new ComparisonResult();
        result.setResponses(responses);
        result.setAnalysis(analysis);

        return result;
    }

    private Map<String, Integer> analyzeCommonWords(List<LlmResponse> responses) {
        Map<String, Integer> wordCount = new HashMap<>();

        for (LlmResponse response : responses) {
            String[] words = response.getResponse().toLowerCase()
                    .replaceAll("[^a-zA-Z\\s]", "")
                    .split("\\s+");

            for (String word : words) {
                if (word.length() > 3) { // Only count words longer than 3 characters
                    wordCount.merge(word, 1, Integer::sum);
                }
            }
        }

        // Return top 10 common words
        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }
}
