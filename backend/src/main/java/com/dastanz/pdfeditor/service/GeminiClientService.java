package com.dastanz.pdfeditor.service;

import com.dastanz.pdfeditor.dto.ChatDocumentResponseDto;
import com.dastanz.pdfeditor.dto.RewriteRequestDto;
import com.dastanz.pdfeditor.dto.RewriteResponseDto;
import com.dastanz.pdfeditor.dto.TranslateRequestDto;
import com.dastanz.pdfeditor.dto.TranslateResponseDto;
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

    @SuppressWarnings("null")
    public TranslateResponseDto translateText(TranslateRequestDto request) {
        String prompt = "Translate the following text to " + request.getTargetLanguage() + ".\n"
            + "Maintain the exact same tone and formatting.\n"
            + "Original Text: " + request.getOriginalText();

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        try {
            String responseStr = restClient.post()
                .body(requestBody)
                .retrieve()
                .body(String.class);

            var root = objectMapper.readTree(responseStr);
            String translated = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            TranslateResponseDto response = new TranslateResponseDto();
            response.setTranslatedText(translated.trim());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to translate text via Gemini API", e);
        }
    }

    @SuppressWarnings("null")
    public ChatDocumentResponseDto chatWithDocument(String documentText, String question) {
        String prompt = "You are a specialized AI assistant analyzing a document.\n"
            + "Use the provided document text ONLY to answer the following question.\n"
            + "If the answer is not in the document, say 'I cannot find the answer in the document.'\n\n"
            + "--- DOCUMENT CONTENT START ---\n"
            + documentText + "\n"
            + "--- DOCUMENT CONTENT END ---\n\n"
            + "Question/Task: " + question;

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        try {
            String responseStr = restClient.post()
                .body(requestBody)
                .retrieve()
                .body(String.class);

            var root = objectMapper.readTree(responseStr);
            String answer = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            ChatDocumentResponseDto response = new ChatDocumentResponseDto();
            response.setAnswer(answer.trim());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback for document too large error or Gemini API failures
            throw new RuntimeException("Failed to process chat via Gemini API. The document may be too large.", e);
        }
    }
}
