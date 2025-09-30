package com.mogou.service;

import java.io.InputStream;

public interface FileStorageService {

    /**
     * Uploade un fichier vers le service de stockage.
     *
     * @param inputStream Le flux de données du fichier.
     * @param objectName  Le nom unique de l'objet à stocker.
     * @param contentType Le type de contenu du fichier.
     */
    void uploadFile(InputStream inputStream, String objectName, String contentType);

    /**
     * Télécharge un fichier depuis le service de stockage.
     *
     * @param objectName Le nom de l'objet à télécharger.
     * @return Un tableau d'octets contenant les données du fichier.
     */
    byte[] downloadFile(String objectName);
}