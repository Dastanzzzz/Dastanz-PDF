package com.dastanz.pdfeditor.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class DocumentService {

    private final Path tempDir;

    public DocumentService() throws IOException {
        tempDir = Files.createTempDirectory("pdfeditor_docs");
    }

    public String saveDocument(MultipartFile file) throws IOException {
        String documentId = UUID.randomUUID().toString();
        Path targetPath = tempDir.resolve(documentId + ".pdf");
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return documentId;
    }

    public InputStream getDocumentInputStream(String documentId) throws IOException {
        return new FileInputStream(getDocumentFile(documentId));
    }

    public File getDocumentFile(String documentId) throws IOException {
        File file = tempDir.resolve(documentId + ".pdf").toFile();
        if (!file.exists()) {
            throw new IOException("Document not found: " + documentId);
        }
        return file;
    }
}
