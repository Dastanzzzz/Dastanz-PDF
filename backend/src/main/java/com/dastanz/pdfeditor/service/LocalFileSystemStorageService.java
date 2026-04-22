package com.dastanz.pdfeditor.service;

import com.dastanz.pdfeditor.model.DocumentMeta;
import com.dastanz.pdfeditor.repository.DocumentMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileSystemStorageService implements StorageService {

    private final DocumentMetaRepository repository;
    private final Path rootLocation = Path.of(System.getProperty("java.io.tmpdir"), "pdfeditor-uploads");

    public LocalFileSystemStorageService(DocumentMetaRepository repository) {
        this.repository = repository;
    }

    @Override
    public String store(MultipartFile file) {
        init();
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
            String storedFilename = UUID.randomUUID().toString() + ".pdf";
            Path destinationFile = rootLocation.resolve(Paths.get(storedFilename)).normalize().toAbsolutePath();
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            DocumentMeta meta = new DocumentMeta();
            meta.setOriginalFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.pdf");
            meta.setStoredFilename(storedFilename);
            meta.setStorageLocation(destinationFile.toString());
            meta.setFileSize(file.getSize());
            meta.setContentType(file.getContentType());
            
            repository.save(meta);

            return storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public String store(InputStream inputStream, String originalFilename, String contentType) {
         init();
         try {
             String storedFilename = UUID.randomUUID().toString() + ".pdf";
             Path destinationFile = rootLocation.resolve(Paths.get(storedFilename)).normalize().toAbsolutePath();
             
             long size = Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
             
             DocumentMeta meta = new DocumentMeta();
             meta.setOriginalFilename(originalFilename);
             meta.setStoredFilename(storedFilename);
             meta.setStorageLocation(destinationFile.toString());
             meta.setFileSize(size);
             meta.setContentType(contentType);
             
             repository.save(meta);
             
             return storedFilename;
         } catch (IOException e) {
             throw new RuntimeException("Failed to store from input stream.", e);
         }
    }

    @Override
    public File loadAsFile(String storedFilename) {
        return rootLocation.resolve(storedFilename).toFile();
    }

    @Override
    public void delete(String storedFilename) {
        try {
            Files.deleteIfExists(rootLocation.resolve(storedFilename));
            // Optional: delete from repo as well if needed.
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete stored file.", e);
        }
    }

    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }
}
