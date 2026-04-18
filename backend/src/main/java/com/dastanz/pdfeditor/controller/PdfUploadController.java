package com.dastanz.pdfeditor.controller;

import com.dastanz.pdfeditor.dto.UploadResponseDto;
import com.dastanz.pdfeditor.model.PageTextBlock;
import com.dastanz.pdfeditor.service.DocumentService;
import com.dastanz.pdfeditor.service.PdfTextBlockExtractorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class PdfUploadController {

    private final DocumentService documentService;
    private final PdfTextBlockExtractorService extractorService;

    public PdfUploadController(DocumentService documentService, PdfTextBlockExtractorService extractorService) {
        this.documentService = documentService;
        this.extractorService = extractorService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDto> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Starting PDF upload for file: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize() + " bytes");
            
            String documentId = documentService.saveDocument(file);
            System.out.println("Document saved with ID: " + documentId);
            
            try (InputStream is = documentService.getDocumentInputStream(documentId)) {
                System.out.println("Extracting text blocks from PDF...");
                List<PageTextBlock> blocks = extractorService.extractTextBlocks(is);
                System.out.println("Successfully extracted " + blocks.size() + " text blocks");
                
                UploadResponseDto response = new UploadResponseDto(documentId, blocks);
                return ResponseEntity.ok(response);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            System.err.println("IO Error during PDF upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).build();
        } catch (Exception e) {
            System.err.println("Unexpected error during PDF upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
