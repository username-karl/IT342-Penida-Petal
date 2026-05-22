package com.petal.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "petal.storage.provider", havingValue = "supabase")
public class SupabaseStorageService implements StorageService {

    private static final String OBJECT_REFERENCE_PREFIX = "supabase://";

    private final RestClient restClient;
    private final StorageProperties properties;

    public SupabaseStorageService(RestClient.Builder restClientBuilder, StorageProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(requireConfig(properties.supabaseUrl(), "petal.storage.supabase-url"))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + requireConfig(properties.supabaseServiceKey(), "petal.storage.supabase-service-key"))
                .defaultHeader("apikey", requireConfig(properties.supabaseServiceKey(), "petal.storage.supabase-service-key"))
                .build();
    }

    @Override
    public String storeOrderPhoto(MultipartFile file, String prefix) {
        String objectPath = StorageObjectNames.safeObjectName(file, prefix);
        upload(properties.orderPhotosBucket(), objectPath, file);
        return OBJECT_REFERENCE_PREFIX + properties.orderPhotosBucket() + "/" + objectPath;
    }

    @Override
    public String storeFloristLogo(MultipartFile file) {
        String objectPath = StorageObjectNames.safeObjectName(file, "logo");
        upload(properties.floristLogosBucket(), objectPath, file);
        return properties.supabaseUrl() + "/storage/v1/object/public/"
                + encodePath(properties.floristLogosBucket()) + "/" + encodePath(objectPath);
    }

    @Override
    public String resolveUrl(String storedPath) {
        if (!StringUtils.hasText(storedPath) || !storedPath.startsWith(OBJECT_REFERENCE_PREFIX)) {
            return storedPath;
        }
        StorageObject object = parseReference(storedPath);
        SignedUrlResponse response = restClient.post()
                .uri("/storage/v1/object/sign/{bucket}/{path}", object.bucket(), object.path())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("expiresIn", properties.signedUrlExpiresSeconds()))
                .retrieve()
                .body(SignedUrlResponse.class);
        if (response == null || !StringUtils.hasText(response.signedURL())) {
            throw new IllegalStateException("Supabase did not return a signed URL");
        }
        if (response.signedURL().startsWith("http://") || response.signedURL().startsWith("https://")) {
            return response.signedURL();
        }
        return properties.supabaseUrl() + response.signedURL();
    }

    private void upload(String bucket, String objectPath, MultipartFile file) {
        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return objectPath;
                }
            };
            restClient.put()
                    .uri("/storage/v1/object/{bucket}/{path}", bucket, objectPath)
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(resource)
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read uploaded image", ex);
        }
    }

    private StorageObject parseReference(String storedPath) {
        String reference = storedPath.substring(OBJECT_REFERENCE_PREFIX.length());
        int slash = reference.indexOf('/');
        if (slash <= 0 || slash == reference.length() - 1) {
            throw new IllegalArgumentException("Invalid Supabase storage reference");
        }
        return new StorageObject(reference.substring(0, slash), reference.substring(slash + 1));
    }

    private String encodePath(String value) {
        return UriUtils.encodePath(value, StandardCharsets.UTF_8);
    }

    private static String requireConfig(String value, String propertyName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(propertyName + " is required when petal.storage.provider=supabase");
        }
        return value;
    }

    private record StorageObject(String bucket, String path) {
    }

    private record SignedUrlResponse(String signedURL) {
    }
}
