package com.example.springai.service;

import com.example.springai.dto.ComparisonResult;

public interface ReportService {
    String generateMarkdownReport(ComparisonResult result);
    String generateJsonReport(ComparisonResult result);
}
