package com.dastanz.pdfeditor.service;

import com.dastanz.pdfeditor.model.DocumentMeta;
import com.dastanz.pdfeditor.repository.DocumentMetaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class AwsS3StorageService implements StorageService {

    private final S3Client s3Client;
    private final DocumentMetaRepository repository;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    public AwsS3StorageService(S3Client s3Client, DocumentMetaRepository repository) {
        this.s3Client = s3Client;
        this.repository = repository;
    }

    @Override
    public String store(MultipartFile file) {
        try {
            return store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file for S3 upload", e);
        }
    }

    @Override
    public String store(InputStream inputStream, String originalFilename, String contentType) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        UUID fileId = UUID.randomUUID();
        String storedFilename = fileId.toString() + ext;

        try {
            long contentLength = inputStream.available(); // Note: in real-world you might need exact length. Using available is a fallback.
            
            // If the stream doesn't support mark/reset or available isn't reliable, better buffer it.
            // But we will use a simple PutObject here for demonstration.
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedFilename)
                    .contentType(contentType != null ? contentType : "application/pdf")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));

            DocumentMeta meta = new DocumentMeta();
            meta.setId(fileId);
            meta.setOriginalFilename(originalFilename);
            repository.save(meta);

            return storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to stream data to S3", e);
        }
    }

    @Override
    public File loadAsFile(String storedFilename) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedFilename)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            
            // PDFBox requires a physical File for many operations, so we cache it temporarily.
            File tempFile = File.createTempFile("s3-cache-", "-" + storedFilename);
            Files.copy(s3Object, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.deleteOnExit(); // cleans up eventually
            
            return tempFile;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from S3: " + storedFilename, e);
        }
    }

    @Override
    public void delete(String storedFilename) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedFilename)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            
            // Extract UUID from storedFilename to delete from database
            String idStr = storedFilename;
            if (storedFilename.contains(".")) {
                idStr = storedFilename.substring(0, storedFilename.lastIndexOf("."));
            }
            repository.deleteById(UUID.fromString(idStr));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + storedFilename, e);
        }
    }
}
