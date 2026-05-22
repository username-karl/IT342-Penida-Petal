package com.petal.service;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

final class StorageObjectNames {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".webp");
    private static final long MAX_BYTES = 5L * 1024L * 1024L;

    private StorageObjectNames() {
    }

    static String safeObjectName(MultipartFile file, String prefix) {
        validate(file);
        String safePrefix = StringUtils.hasText(prefix) ? prefix.replaceAll("[^a-zA-Z0-9-]", "") : "image";
        if (!StringUtils.hasText(safePrefix)) {
            safePrefix = "image";
        }
        return safePrefix + "-" + UUID.randomUUID() + extensionFor(file);
    }

    private static void validate(MultipartFile file) {
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

    private static String extensionFor(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (StringUtils.hasText(original)) {
            String cleaned = StringUtils.cleanPath(original);
            int dot = cleaned.lastIndexOf('.');
            if (dot >= 0 && dot < cleaned.length() - 1) {
                String extension = cleaned.substring(dot).toLowerCase(Locale.ROOT);
                if (ALLOWED_EXTENSIONS.contains(extension)) {
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
