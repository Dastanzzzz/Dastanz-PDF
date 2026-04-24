package com.dastanz.pdfeditor.controller;

import com.dastanz.pdfeditor.dto.ChatDocumentRequestDto;
import com.dastanz.pdfeditor.dto.ChatDocumentResponseDto;
import com.dastanz.pdfeditor.dto.RewriteRequestDto;
import com.dastanz.pdfeditor.dto.RewriteResponseDto;
import com.dastanz.pdfeditor.dto.TranslateRequestDto;
import com.dastanz.pdfeditor.dto.TranslateResponseDto;
import com.dastanz.pdfeditor.service.DocumentService;
import com.dastanz.pdfeditor.service.GeminiClientService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/edit")
public class TextEditController {

    private final GeminiClientService geminiClientService;
    private final DocumentService documentService;

    public TextEditController(GeminiClientService geminiClientService, DocumentService documentService) {
        this.geminiClientService = geminiClientService;
        this.documentService = documentService;
    }

    @PostMapping("/rewrite")
    public ResponseEntity<RewriteResponseDto> rewriteText(@RequestBody RewriteRequestDto request) {
        RewriteResponseDto response = geminiClientService.rewriteText(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate")
    public ResponseEntity<TranslateResponseDto> translateText(@RequestBody TranslateRequestDto request) {
        TranslateResponseDto response = geminiClientService.translateText(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatDocumentResponseDto> chatWithDocument(@RequestBody ChatDocumentRequestDto request) {
        try {
            File pdfFile = documentService.getDocumentFile(request.getDocumentId());
            String documentText = "";
            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                documentText = stripper.getText(document);
            }
            
            // Limit text size conditionally if too large, but for now we pass to Gemini
            if (documentText.length() > 50000) {
                // simple truncate to 50k chars to avoid crazy payload size
                documentText = documentText.substring(0, 50000);
            }
            
            ChatDocumentResponseDto response = geminiClientService.chatWithDocument(documentText, request.getQuestion());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
