package com.dastanz.pdfeditor.controller;

import com.dastanz.pdfeditor.service.DocumentService;
import com.dastanz.pdfeditor.service.FluidEditorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/api/fluid")
public class FluidEditorController {

    private final DocumentService documentService;
    private final FluidEditorService fluidEditorService;

    public FluidEditorController(DocumentService documentService, FluidEditorService fluidEditorService) {
        this.documentService = documentService;
        this.fluidEditorService = fluidEditorService;
    }

    @GetMapping("/{documentId}/text")
    public ResponseEntity<java.util.Map<String, String>> getFullText(@PathVariable String documentId) {
        try {
            System.out.println("Attempting to load fluid text for document: " + documentId);
            File pdfFile = documentService.getDocumentFile(documentId);
            System.out.println("PDF file path: " + pdfFile.getAbsolutePath());
            System.out.println("PDF file exists: " + pdfFile.exists());
            System.out.println("PDF file size: " + pdfFile.length() + " bytes");
            
            String text = fluidEditorService.extractFullText(pdfFile);
            System.out.println("Successfully extracted text, length: " + (text != null ? text.length() : 0));

            // Map.of doesn't allow null values, so we use a safer approach
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("text", text != null ? text : "");

            return ResponseEntity.ok(response);
        } catch (java.io.IOException e) {
            System.err.println("IO Error extracting fluid text for document: " + documentId);
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("error", "File not found or cannot be read: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error extracting fluid text for document: " + documentId);
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Failed to extract text: " + e.getMessage()));
        }
    }

    @SuppressWarnings("null")
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportFluidPdf(@RequestBody Map<String, String> payload) {
        try {
            String htmlContent = payload.get("htmlContent");
            
            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                System.err.println("Export failed: No HTML content provided");
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("Attempting to export PDF, HTML content length: " + htmlContent.length());
            byte[] pdfBytes = fluidEditorService.generatePdfFromHtml(htmlContent);
            
            if (pdfBytes == null || pdfBytes.length == 0) {
                System.err.println("Export failed: Generated PDF is empty");
                return ResponseEntity.status(500).build();
            }
            
            System.out.println("PDF exported successfully, size: " + pdfBytes.length + " bytes");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fluid_edited.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("Export failed with exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
