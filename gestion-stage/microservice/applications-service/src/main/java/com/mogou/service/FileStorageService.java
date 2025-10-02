package com.mogou.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final MinioClient minioClient;
    private boolean minioAvailable = false;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public FileStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            minioAvailable = true;
            logger.info("MinIO initialized successfully with bucket: {}", bucketName);
        } catch (Exception e) {
            logger.warn("MinIO not available at startup: {}. Service will run in degraded mode.", e.getMessage());
            minioAvailable = false;
        }
    }

    public String uploadFile(MultipartFile file, Long etudiantId) {
        if (!minioAvailable) {
            logger.warn("MinIO not available, file upload skipped for student: {}", etudiantId);
            return "file-upload-disabled-" + UUID.randomUUID();
        }
        
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File is empty or null");
            }
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // Création d'un chemin structuré pour mieux organiser les fichiers
            String uniqueFileName = "etudiant-" + etudiantId + "/" + UUID.randomUUID() + fileExtension;
            
            logger.info("Uploading file to MinIO: {} (size: {} bytes)", uniqueFileName, file.getSize());

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            
            logger.info("File uploaded successfully: {}", uniqueFileName);
            return uniqueFileName;
        } catch (Exception e) {
            logger.error("Error uploading file for student {}: {}", etudiantId, e.getMessage(), e);
            throw new RuntimeException("Error occurred while uploading file: " + e.getMessage(), e);
        }
    }

    /**
     * Télécharge un fichier depuis MinIO.
     * @param filePath Le chemin complet du fichier dans le bucket.
     * @return Le contenu du fichier en tant que tableau d'octets.
     */
    public byte[] downloadFile(String filePath) {
        if (!minioAvailable) {
            logger.warn("MinIO not available, file download failed for: {}", filePath);
            throw new RuntimeException("File storage service not available");
        }
        
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .build())) {
            return IOUtils.toByteArray(stream);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while downloading file: " + filePath, e);
        }
    }
    
    public boolean isMinioAvailable() {
        return minioAvailable;
    }
}


