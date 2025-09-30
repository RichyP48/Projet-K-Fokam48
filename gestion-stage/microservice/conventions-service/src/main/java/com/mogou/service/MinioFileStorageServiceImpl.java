package com.mogou.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service

public class MinioFileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioFileStorageServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * S'assure que le bucket existe au démarrage de l'application.
     */
    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("✅ Bucket MinIO '" + bucketName + "' créé.");
            } else {
                System.out.println("✅ Bucket MinIO '" + bucketName + "' existe déjà.");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation du bucket MinIO: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'initialisation du bucket MinIO.", e);
        }
    }

    @Override
    public void uploadFile(InputStream inputStream, String objectName, String contentType) {
        try {
            long size = inputStream.available();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build());
            System.out.println("✅ Fichier uploadé vers MinIO: " + objectName + " (" + size + " bytes)");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'upload du fichier '" + objectName + "' vers MinIO: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'upload du fichier vers MinIO: " + objectName, e);
        }
    }

    @Override
    public byte[] downloadFile(String objectName) {
        try {
            // Vérifier si l'objet existe
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build())) {
                byte[] data = IOUtils.toByteArray(stream);
                System.out.println("✅ Fichier téléchargé depuis MinIO: " + objectName + " (" + data.length + " bytes)");
                return data;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du téléchargement du fichier '" + objectName + "' depuis MinIO: " + e.getMessage());
            throw new RuntimeException("Erreur lors du téléchargement du fichier depuis MinIO: " + objectName, e);
        }
    }
}
