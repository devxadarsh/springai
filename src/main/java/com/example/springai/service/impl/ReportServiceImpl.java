package com.example.springai.service.impl;

// Report Service Implementation

import com.example.springai.dto.ComparisonResult;
import com.example.springai.model.LlmResponse;
import com.example.springai.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateMarkdownReport(ComparisonResult result) {
        try {
            // Create reports directory if it doesn't exist
            Path reportsDir = Paths.get("reports");
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = String.format("llm_comparison_%s.md", timestamp);
            Path filePath = reportsDir.resolve(filename);

            StringBuilder markdown = new StringBuilder();

            // Header
            markdown.append("# LLM Comparison Report\n\n");
            markdown.append(String.format("**Generated on:** %s\n\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            markdown.append(String.format("**Session ID:** %s\n\n", result.getSessionId()));

            // Prompt
            markdown.append("## Prompt\n\n");
            markdown.append("```\n").append(result.getPrompt()).append("\n```\n\n");

            // Analysis Summary
            if (result.getAnalysis() != null) {
                markdown.append("## Analysis Summary\n\n");
                Map<String, Object> analysis = result.getAnalysis();

                markdown.append("### Performance Metrics\n\n");
                markdown.append(String.format("- **Total Responses:** %s\n", analysis.get("totalResponses")));
                markdown.append(String.format("- **Average Response Time:** %.2f ms\n", (Double) analysis.get("averageResponseTime")));
                markdown.append(String.format("- **Fastest Provider:** %s (%.0f ms)\n",
                        analysis.get("fastestProvider"), analysis.get("fastestTime")));
                markdown.append(String.format("- **Slowest Provider:** %s (%.0f ms)\n",
                        analysis.get("slowestProvider"), analysis.get("slowestTime")));

                markdown.append("\n### Content Metrics\n\n");
                markdown.append(String.format("- **Average Response Length:** %.0f characters\n", (Double) analysis.get("averageResponseLength")));
                markdown.append(String.format("- **Shortest Response:** %s (%s characters)\n",
                        analysis.get("shortestProvider"), analysis.get("shortestLength")));
                markdown.append(String.format("- **Longest Response:** %s (%s characters)\n",
                        analysis.get("longestProvider"), analysis.get("longestLength")));
                markdown.append(String.format("- **Average Tokens:** %.0f\n", (Double) analysis.get("averageTokens")));

                // Common words
                @SuppressWarnings("unchecked")
                Map<String, Integer> commonWords = (Map<String, Integer>) analysis.get("commonWords");
                if (!commonWords.isEmpty()) {
                    markdown.append("\n### Most Common Words\n\n");
                    commonWords.forEach((word, count) ->
                            markdown.append(String.format("- **%s:** %d occurrences\n", word, count)));
                }
                markdown.append("\n");
            }

            // Individual Responses
            markdown.append("## Detailed Responses\n\n");

            for (int i = 0; i < result.getResponses().size(); i++) {
                LlmResponse response = result.getResponses().get(i);

                markdown.append(String.format("### %d. %s (%s)\n\n",
                        i + 1, response.getProvider().getDisplayName(), response.getModel()));

                markdown.append("**Metadata:**\n");
                markdown.append(String.format("- Response Time: %d ms\n", response.getResponseTimeMs()));
                markdown.append(String.format("- Token Count: %s\n", response.getTokenCount() != null ? response.getTokenCount() : "N/A"));
                markdown.append(String.format("- Response Length: %d characters\n", response.getResponse().length()));
                markdown.append(String.format("- Timestamp: %s\n\n", response.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

                markdown.append("**Response:**\n\n");
                markdown.append("```\n").append(response.getResponse()).append("\n```\n\n");

                markdown.append("---\n\n");
            }

            // Comparison Analysis
            markdown.append("## Comparison Analysis\n\n");
            markdown.append("### Key Differences\n\n");

            if (result.getResponses().size() >= 2) {
                markdown.append("**Response Style Comparison:**\n");
                for (LlmResponse response : result.getResponses()) {
                    markdown.append(String.format("- **%s**: ", response.getProvider().getDisplayName()));

                    String resp = response.getResponse().toLowerCase();
                    if (resp.contains("example") || resp.contains("for instance")) {
                        markdown.append("Uses examples frequently. ");
                    }
                    if (resp.contains("step") || resp.contains("first") || resp.contains("second")) {
                        markdown.append("Structured/step-by-step approach. ");
                    }
                    if (resp.length() > 1000) {
                        markdown.append("Detailed explanation. ");
                    } else if (resp.length() < 500) {
                        markdown.append("Concise response. ");
                    } else {
                        markdown.append("Moderate length response. ");
                    }
                    markdown.append("\n");
                }
                markdown.append("\n");
            }

            markdown.append("### Recommendations\n\n");
            if (result.getAnalysis() != null) {
                String fastestProvider = (String) result.getAnalysis().get("fastestProvider");
                String longestProvider = (String) result.getAnalysis().get("longestProvider");

                markdown.append(String.format("- **For Speed:** %s provided the fastest response\n", fastestProvider));
                markdown.append(String.format("- **For Detail:** %s provided the most comprehensive response\n", longestProvider));
                markdown.append("- **Overall:** Consider the trade-off between response time and detail based on your use case\n\n");
            }

            // Footer
            markdown.append("---\n");
            markdown.append("*Report generated by LLM Comparator Spring Boot Application*\n");

            // Write to file
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                writer.write(markdown.toString());
            }

            logger.info("Markdown report generated: {}", filePath.toAbsolutePath());
            return filePath.toAbsolutePath().toString();

        } catch (IOException e) {
            logger.error("Error generating markdown report: ", e);
            throw new RuntimeException("Failed to generate markdown report", e);
        }
    }

    @Override
    public String generateJsonReport(ComparisonResult result) {
        try {
            Path reportsDir = Paths.get("reports");
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = String.format("llm_comparison_%s.json", timestamp);
            Path filePath = reportsDir.resolve(filename);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), result);

            logger.info("JSON report generated: {}", filePath.toAbsolutePath());
            return filePath.toAbsolutePath().toString();

        } catch (IOException e) {
            logger.error("Error generating JSON report: ", e);
            throw new RuntimeException("Failed to generate JSON report", e);
        }
    }
}
