package com.petal.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@ConditionalOnProperty(name = "petal.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path uploadRoot;

    public LocalStorageService(@Value("${petal.storage.local-upload-dir:${petal.upload-dir:uploads}}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String storeOrderPhoto(MultipartFile file, String prefix) {
        return store(file, prefix, "order-photos", "/uploads/order-photos/", "order photo");
    }

    @Override
    public String storeFloristLogo(MultipartFile file) {
        return store(file, "logo", "florist-logos", "/uploads/florist-logos/", "florist logo");
    }

    @Override
    public String resolveUrl(String storedPath) {
        return storedPath;
    }

    private String store(MultipartFile file, String prefix, String directory, String publicPath, String uploadLabel) {
        String filename = StorageObjectNames.safeObjectName(file, prefix);
        Path targetRoot = uploadRoot.resolve(directory).normalize();
        try {
            Files.createDirectories(targetRoot);
            Path destination = targetRoot.resolve(filename).normalize();
            if (!destination.startsWith(targetRoot)) {
                throw new IllegalArgumentException("Invalid upload path");
            }
            file.transferTo(destination);
            return publicPath + filename;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store " + uploadLabel, ex);
        }
    }
}
