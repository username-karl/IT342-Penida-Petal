package com.petal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");
    private static final long MAX_BYTES = 5L * 1024L * 1024L;

    private final Path uploadRoot;

    public OrderImageStorageService(@Value("${petal.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize().resolve("order-photos");
    }

    public String store(MultipartFile file, String prefix) {
        validate(file);
        try {
            Files.createDirectories(uploadRoot);
            String extension = extensionFor(file);
            String safePrefix = StringUtils.hasText(prefix) ? prefix.replaceAll("[^a-zA-Z0-9-]", "") : "order";
            String filename = safePrefix + "-" + UUID.randomUUID() + extension;
            Path destination = uploadRoot.resolve(filename).normalize();
            if (!destination.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Invalid upload path");
            }
            file.transferTo(destination);
            return "/uploads/order-photos/" + filename;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store order photo", ex);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo file is required");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, or WEBP images are allowed");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Photo must be 5MB or smaller");
        }
    }

    private String extensionFor(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (StringUtils.hasText(original)) {
            String cleaned = StringUtils.cleanPath(original);
            int dot = cleaned.lastIndexOf('.');
            if (dot >= 0 && dot < cleaned.length() - 1) {
                String extension = cleaned.substring(dot).toLowerCase(Locale.ROOT);
                if (Set.of(".png", ".jpg", ".jpeg", ".webp").contains(extension)) {
                    return extension;
                }
            }
        }
        if ("image/png".equalsIgnoreCase(file.getContentType())) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(file.getContentType())) {
            return ".webp";
        }
        return ".jpg";
    }
}
