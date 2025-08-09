package com.example.springai.controller;


import com.example.springai.dto.ComparisonRequest;
import com.example.springai.dto.ComparisonResult;
import com.example.springai.model.LlmResponse;
import com.example.springai.service.ComparisonService;
import com.example.springai.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/v1/llm")
@CrossOrigin(origins = "*")
public class LlmComparisonController {
    private final ComparisonService comparisonService;
    private final ReportService reportService;

    @Autowired
    public LlmComparisonController(ComparisonService comparisonService, ReportService reportService) {
        this.comparisonService = comparisonService;
        this.reportService = reportService;
    }

    @PostMapping("/compare")
    public ResponseEntity<ComparisonResult> compareModels(@Valid @RequestBody ComparisonRequest request) {
        ComparisonResult result = comparisonService.compareModels(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<LlmResponse>> getComparisonHistory(@PathVariable String sessionId) {
        List<LlmResponse> history = comparisonService.getComparisonHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/compare/quick")
    public ResponseEntity<ComparisonResult> quickCompare(@RequestParam String prompt) {
        ComparisonRequest request = new ComparisonRequest(prompt);
        ComparisonResult result = comparisonService.compareModels(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/report/json")
    public ResponseEntity<String> generateJsonReport(@RequestBody ComparisonResult result) {
        String reportPath = reportService.generateJsonReport(result);
        return ResponseEntity.ok(reportPath);
    }

    @GetMapping("/report/download/{sessionId}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String sessionId) {
        // This would typically fetch the report path from database
        // For now, we'll return a simple response
        try {
            File file = new File("reports/");
            if (file.exists() && file.listFiles().length > 0) {
                File latestReport = file.listFiles()[file.listFiles().length - 1];
                Resource resource = new FileSystemResource(latestReport);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + latestReport.getName() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            }
        } catch (Exception e) {
            // Handle exception
        }

        return ResponseEntity.notFound().build();
    }
}
