package com.dastanz.pdfeditor.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.InputStream;

public interface StorageService {
    String store(MultipartFile file);
    String store(InputStream inputStream, String originalFilename, String contentType);
    File loadAsFile(String storedFilename);
    void delete(String storedFilename);
}
