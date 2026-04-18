package com.dastanz.pdfeditor.service;

import com.dastanz.pdfeditor.dto.RewriteRequestDto;
import com.dastanz.pdfeditor.dto.RewriteResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiClientService {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiClientService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("null")
    public RewriteResponseDto rewriteText(RewriteRequestDto request) {
        String prompt = "You are a professional PDF text editor assistant.\n"
            + "Given the following original text block from a PDF and the user's instruction, "
            + "rewrite the text accordingly. Keep the context and general length suitable for a PDF document.\n"
            + "Instruction: " + request.getInstruction() + "\n"
            + "Original Text: " + request.getOriginalText() + "\n"
            + "Respond strictly in the following JSON format: {\"edited_text\": \"...\", \"short_reason\": \"...\", \"changed_terms\": [\"...\"]}";

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            ),
            "generationConfig", Map.of(
                "responseMimeType", "application/json"
            )
        );

        try {
            String responseStr = restClient.post()
                .body(requestBody)
                .retrieve()
                .body(String.class);

            // Parse Gemini response
            var root = objectMapper.readTree(responseStr);
            String jsonOutput = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            // Strip markdown formatting if Gemini returns it
            if (jsonOutput.startsWith("```json")) {
                jsonOutput = jsonOutput.substring(7);
            } else if (jsonOutput.startsWith("```")) {
                jsonOutput = jsonOutput.substring(3);
            }
            if (jsonOutput.endsWith("```")) {
                jsonOutput = jsonOutput.substring(0, jsonOutput.length() - 3);
            }
            jsonOutput = jsonOutput.trim();
            
            return objectMapper.readValue(jsonOutput, RewriteResponseDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to rewrite text via Gemini API", e);
        }
    }
}
