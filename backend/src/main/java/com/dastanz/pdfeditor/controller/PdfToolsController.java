package com.dastanz.pdfeditor.controller;

import com.dastanz.pdfeditor.dto.ArrangeRequestDto;
import com.dastanz.pdfeditor.dto.CompareResultDto;
import com.dastanz.pdfeditor.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tools")
public class PdfToolsController {

    private final DocumentService documentService;
    private final PdfCompressService compressService;
    private final PdfPasswordService passwordService;
    private final PdfMergeService mergeService;
    private final PdfWatermarkService watermarkService;
    private final PdfStampService stampService;
    private final PdfSplitService splitService;
    private final PdfConvertService convertService;
    private final PdfArrangeService arrangeService;
    private final PdfCompareService compareService;
    private final PdfRedactService redactService;
    private final PdfOcrService ocrService;
    private final PdfSignService signService;
    private final PdfFontService fontService;

    public PdfToolsController(DocumentService documentService,
                               PdfCompressService compressService,
                               PdfPasswordService passwordService,
                               PdfMergeService mergeService,
                               PdfWatermarkService watermarkService,
                               PdfStampService stampService,
                               PdfSplitService splitService,
                               PdfConvertService convertService,
                               PdfArrangeService arrangeService,
                               PdfCompareService compareService,
                               PdfRedactService redactService,
                               PdfOcrService ocrService,
                               PdfSignService signService,
                               PdfFontService fontService) {
        this.documentService = documentService;
        this.compressService = compressService;
        this.passwordService = passwordService;
        this.mergeService = mergeService;
        this.watermarkService = watermarkService;
        this.stampService = stampService;
        this.splitService = splitService;
        this.convertService = convertService;
        this.arrangeService = arrangeService;
        this.compareService = compareService;
        this.redactService = redactService;
        this.ocrService = ocrService;
        this.signService = signService;
        this.fontService = fontService;
    }

    // ─── Font Extraction (V2 Preview) ─────────────────────────

    @GetMapping("/extract-fonts")
    public ResponseEntity<List<PdfFontService.FontMetadata>> extractFonts(@RequestParam("documentId") String documentId) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            List<PdfFontService.FontMetadata> fonts = fontService.extractFontMetadata(pdfFile);
            return ResponseEntity.ok(fonts);
        } catch (Exception e) {
            System.err.println("Font extraction failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Sign ──────────────────────────────────────────────────

    @PostMapping("/sign")
    public ResponseEntity<byte[]> signPdf(
            @RequestParam("documentId") String documentId,
            @RequestParam("signatureFile") MultipartFile signatureFile,
            @RequestParam("page") int page,
            @RequestParam("x") float x,
            @RequestParam("y") float y,
            @RequestParam("scale") float scale) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            byte[] signatureBytes = signatureFile.getBytes();
            byte[] result = signService.sign(pdfFile, signatureBytes, page, x, y, scale);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=signed.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (Exception e) {
            System.err.println("Sign failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Compress ────────────────────────────────────────────────

    @PostMapping("/compress")
    public ResponseEntity<byte[]> compressPdf(
            @RequestParam("documentId") String documentId,
            @RequestParam(value = "quality", defaultValue = "medium") String quality) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            byte[] result = compressService.compress(pdfFile, quality);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (Exception e) {
            System.err.println("Compress failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Password ────────────────────────────────────────────────

    @PostMapping("/password")
    public ResponseEntity<byte[]> addPassword(
            @RequestParam("documentId") String documentId,
            @RequestParam("userPassword") String userPassword,
            @RequestParam(value = "ownerPassword", required = false) String ownerPassword,
            @RequestParam(value = "allowPrint", defaultValue = "true") boolean allowPrint,
            @RequestParam(value = "allowCopy", defaultValue = "false") boolean allowCopy) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            byte[] result = passwordService.addPassword(pdfFile, userPassword, ownerPassword, allowPrint, allowCopy);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=protected.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (Exception e) {
            System.err.println("Password protection failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Merge ───────────────────────────────────────────────────

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length < 2) {
            return ResponseEntity.badRequest().build();
        }

        List<InputStream> streams = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                streams.add(file.getInputStream());
            }
            byte[] result = mergeService.merge(streams);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (Exception e) {
            System.err.println("Merge failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        } finally {
            for (InputStream is : streams) {
                try { is.close(); } catch (Exception ignored) {}
            }
        }
    }

    // ─── Watermark ───────────────────────────────────────────────

    @PostMapping("/watermark")
    public ResponseEntity<byte[]> addWatermark(
            @RequestParam("documentId") String documentId,
            @RequestParam("text") String text,
            @RequestParam(value = "fontSize", defaultValue = "48") float fontSize,
            @RequestParam(value = "opacity", defaultValue = "0.3") float opacity,
            @RequestParam(value = "rotation", defaultValue = "45") float rotation,
            @RequestParam(value = "color", defaultValue = "#CCCCCC") String color) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            byte[] result = watermarkService.addWatermark(pdfFile, text, fontSize, opacity, rotation, color);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=watermarked.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (Exception e) {
            System.err.println("Watermark failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Stamp ───────────────────────────────────────────────────

    @PostMapping("/stamp")
    public ResponseEntity<byte[]> addStamp(
            @RequestParam("documentId") String documentId,
            @RequestParam("stampImage") MultipartFile stampImage,
            @RequestParam(value = "position", defaultValue = "center") String position,
            @RequestParam(value = "scale", defaultValue = "0.5") float scale,
            @RequestParam(value = "opacity", defaultValue = "0.8") float opacity,
            @RequestParam(value = "pageSelection", defaultValue = "all") String pageSelection) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            byte[] stampBytes = stampImage.getBytes();
            byte[] result = stampService.addStamp(pdfFile, stampBytes, position, scale, opacity, pageSelection);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stamped.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (Exception e) {
            System.err.println("Stamp failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Split ──────────────────────────────────────────────────

    @PostMapping("/split")
    public ResponseEntity<byte[]> splitPdf(
            @RequestParam("documentId") String documentId,
            @RequestParam("pages") String pages) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            byte[] result = splitService.split(pdfFile, pages);

            if (result == null || result.length == 0) {
                 return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=split.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (IllegalArgumentException e) {
            System.err.println("Split validation error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Split failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Arrange ────────────────────────────────────────────────

    @PostMapping("/arrange")
    public ResponseEntity<byte[]> arrangePdf(@RequestBody ArrangeRequestDto request) {
        try {
            File pdfFile = documentService.getDocumentFile(request.getDocumentId());
            byte[] result = arrangeService.arrange(pdfFile, request.getPages());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=arranged.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (IllegalArgumentException e) {
            System.err.println("Arrange validation error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Arrange failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Convert ────────────────────────────────────────────────

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convertPdf(
            @RequestParam("documentId") String documentId,
            @RequestParam(value = "format", defaultValue = "png") String format,
            @RequestParam(value = "dpi", defaultValue = "150") int dpi) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            String normalizedFormat = format == null ? "png" : format.toLowerCase().trim();

            if ("docx".equals(normalizedFormat) || "word".equals(normalizedFormat)) {
                byte[] result = convertService.convertToWord(pdfFile);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.docx")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                        .body(result);
            }

            byte[] result = convertService.convertToImages(pdfFile, normalizedFormat, dpi);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(result);
        } catch (Exception e) {
            System.err.println("Convert failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Compare ────────────────────────────────────────────────

    @PostMapping("/compare")
    public ResponseEntity<CompareResultDto> comparePdfs(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2) {
        try (InputStream is1 = file1.getInputStream();
             InputStream is2 = file2.getInputStream()) {

            var diffs = compareService.compare(is1, is2);
            return ResponseEntity.ok(new CompareResultDto(diffs));
        } catch (Exception e) {
            System.err.println("Compare failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Redact ─────────────────────────────────────────────────

    @PostMapping("/hide-text")
    public ResponseEntity<byte[]> redactPdf(
            @RequestParam("documentId") String documentId,
            @RequestParam("searchText") String searchText,
            @RequestParam(value = "color", defaultValue = "#000000") String color) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            byte[] result = redactService.redact(pdfFile, searchText, color);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=redacted.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (IllegalArgumentException e) {
            System.err.println("Redact validation error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Redact failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── OCR ────────────────────────────────────────────────────

    @PostMapping("/ocr")
    public ResponseEntity<byte[]> ocrPdf(
            @RequestParam("documentId") String documentId,
            @RequestParam(value = "language", defaultValue = "eng") String language) {
        try {
            File pdfFile = documentService.getDocumentFile(documentId);
            byte[] result = ocrService.ocr(pdfFile, language);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ocr_output.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(result);
        } catch (Exception e) {
            System.err.println("OCR failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
