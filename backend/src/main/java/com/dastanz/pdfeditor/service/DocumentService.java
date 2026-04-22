package com.dastanz.pdfeditor.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Service
public class DocumentService {

    private final StorageService storageService;

    public DocumentService(StorageService storageService) {
        this.storageService = storageService;
    }

    public String saveDocument(MultipartFile file) throws IOException {
        // Delegate to the new StorageService backed by the Database
        return storageService.store(file);
    }

    public InputStream getDocumentInputStream(String documentId) throws IOException {
        return new FileInputStream(getDocumentFile(documentId));
    }

    public File getDocumentFile(String documentId) throws IOException {
        File file = storageService.loadAsFile(documentId);
        if (!file.exists()) {
            throw new IOException("Document not found: " + documentId);
        }
        return file;
    }

    public Path getDocumentPath(String documentId) throws IOException {
        return getDocumentFile(documentId).toPath();
    }
}
