package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

@Service
public class PdfSignService {

    private CertificateGenerator.KeyPairAndCertificate selfSignedIdentity;

    @PostConstruct
    public void init() {
        try {
            // Generate a fresh PKI signing identity on application startup
            this.selfSignedIdentity = CertificateGenerator.generateSelfSignedCertificate();
            System.out.println("✅ Security Component: Self-Signed X.509 PKI initialized for Digital Signatures.");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize cryptographic keys: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public byte[] sign(File pdfFile, byte[] imageBytes, int page, float xPct, float yPct, float scale) throws IOException {
        return process(pdfFile, imageBytes, page, xPct, yPct, scale, true);
    }

    public byte[] insertImage(File pdfFile, byte[] imageBytes, int page, float xPct, float yPct, float scale) throws IOException {
        return process(pdfFile, imageBytes, page, xPct, yPct, scale, false);
    }

    private byte[] process(File pdfFile, byte[] imageBytes, int page, float xPct, float yPct, float scale, boolean addDigitalSignature) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            // PDFBox uses 0-indexed pages
            if (page < 1 || page > document.getNumberOfPages()) {
                throw new IllegalArgumentException("Invalid page number.");
            }
            PDPage pdPage = document.getPage(page - 1);
            
            PDImageXObject image = PDImageXObject.createFromByteArray(document, imageBytes, "image");
            
            float pdfPageWidth = pdPage.getCropBox().getWidth();
            float pdfPageHeight = pdPage.getCropBox().getHeight();
            
            float finalWidth = image.getWidth() * scale;
            float finalHeight = image.getHeight() * scale;
            
            // Calculate absolute PDF points from frontend percentages (0.0 to 100.0)
            float actualX = pdfPageWidth * (xPct / 100f);
            float actualY = pdfPageHeight * (yPct / 100f);
            
            // Translate Top-Left frontend Y to Bottom-Left PDFBox Y
            float pdfY = pdfPageHeight - actualY - finalHeight;

            try (PDPageContentStream cs = new PDPageContentStream(document, pdPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
                cs.drawImage(image, actualX, pdfY, finalWidth, finalHeight);
            }

            // --- Real Digital Cryptographic Signature Addition ---
            if (addDigitalSignature && selfSignedIdentity != null) {
                PDSignature signature = new PDSignature();
                signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
                signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
                signature.setName("Dastanz AI User");
                signature.setLocation("Online Workspace");
                signature.setReason("Document legally and securely signed");
                signature.setSignDate(Calendar.getInstance());

                // Attach digital signature handler to our PDF
                document.addSignature(signature, new PKISignature(selfSignedIdentity.getPrivateKey(), selfSignedIdentity.getChain()));
                
                // Write securely to output
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                document.saveIncremental(out);
                return out.toByteArray();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
