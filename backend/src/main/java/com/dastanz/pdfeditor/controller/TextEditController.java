package com.dastanz.pdfeditor.controller;

import com.dastanz.pdfeditor.dto.RewriteRequestDto;
import com.dastanz.pdfeditor.dto.RewriteResponseDto;
import com.dastanz.pdfeditor.service.GeminiClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/edit")
public class TextEditController {

    private final GeminiClientService geminiClientService;

    public TextEditController(GeminiClientService geminiClientService) {
        this.geminiClientService = geminiClientService;
    }

    @PostMapping("/rewrite")
    public ResponseEntity<RewriteResponseDto> rewriteText(@RequestBody RewriteRequestDto request) {
        RewriteResponseDto response = geminiClientService.rewriteText(request);
        return ResponseEntity.ok(response);
    }
}
