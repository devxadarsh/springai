package com.example.springai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LlmProvider {
    OPENAI("OpenAI GPT"),
    CLAUDE("Claude"),
    VERTEX_GEMINI("Vertex Gemini");

    private final String displayName;
}
