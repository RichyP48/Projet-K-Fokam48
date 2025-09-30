package com.mogou.config;


import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * Crée et configure un bean MinioClient pour l'injection de dépendances.
     * Spring appellera cette méthode au démarrage pour créer un singleton
     * du client MinIO, qu'il pourra ensuite injecter partout où c'est nécessaire.
     *
     * @return Une instance configurée de MinioClient.
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
