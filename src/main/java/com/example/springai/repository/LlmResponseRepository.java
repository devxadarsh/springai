package com.example.springai.repository;

import com.example.springai.model.LlmResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LlmResponseRepository extends JpaRepository<LlmResponse, Long> {

    List<LlmResponse> findBySessionIdOrderByCreatedAt(String sessionId);

    List<LlmResponse> findByPromptContainingIgnoreCase(String prompt);

    List<LlmResponse> findTop10ByOrderByCreatedAtDesc();
}

