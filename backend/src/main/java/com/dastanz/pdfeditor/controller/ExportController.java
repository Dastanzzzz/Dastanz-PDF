package com.dastanz.pdfeditor.controller;

import com.dastanz.pdfeditor.dto.ExportRequestDto;
import com.dastanz.pdfeditor.service.DocumentService;
import com.dastanz.pdfeditor.service.PdfRegenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequestMapping("/api/pdf")
public class ExportController {

    private final DocumentService documentService;
    private final PdfRegenerationService regenerationService;

    public ExportController(DocumentService documentService, PdfRegenerationService regenerationService) {
        this.documentService = documentService;
        this.regenerationService = regenerationService;
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportPdf(@RequestBody ExportRequestDto request) {
        try (InputStream is = documentService.getDocumentInputStream(request.getDocumentId())) {
            byte[] regeneratedPdf = regenerationService.applyEdits(is, request.getEdits(), request.getToolState());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "edited.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(regeneratedPdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
