package com.petal.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "petal.storage")
public record StorageProperties(
        String provider,
        String supabaseUrl,
        String supabaseServiceKey,
        String floristLogosBucket,
        String orderPhotosBucket,
        int signedUrlExpiresSeconds,
        String localUploadDir) {

    public StorageProperties {
        provider = blankToDefault(provider, "local");
        supabaseUrl = stripTrailingSlash(supabaseUrl);
        floristLogosBucket = blankToDefault(floristLogosBucket, "florist-logos");
        orderPhotosBucket = blankToDefault(orderPhotosBucket, "order-photos");
        signedUrlExpiresSeconds = signedUrlExpiresSeconds <= 0 ? 300 : signedUrlExpiresSeconds;
        localUploadDir = blankToDefault(localUploadDir, "uploads");
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.trim().replaceAll("/+$", "");
    }
}
