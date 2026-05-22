package com.petal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    @TempDir
    private Path uploadDir;

    @Test
    void validOrderImageStoresWithSafeGeneratedPath() throws Exception {
        LocalStorageService storageService = new LocalStorageService(uploadDir.toString());
        MockMultipartFile file = imageFile("../proof.png", "image/png");

        String storedPath = storageService.storeOrderPhoto(file, "proof");

        assertThat(storedPath).startsWith("/uploads/order-photos/proof-").endsWith(".png");
        assertThat(storedPath).doesNotContain("..");
        assertThat(Files.exists(uploadDir.resolve(storedPath.replace("/uploads/", "")))).isTrue();
    }

    @Test
    void emptyImageIsRejected() {
        LocalStorageService storageService = new LocalStorageService(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "proof.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> storageService.storeOrderPhoto(file, "proof"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Photo file is required");
    }

    @Test
    void invalidContentTypeIsRejected() {
        LocalStorageService storageService = new LocalStorageService(uploadDir.toString());
        MockMultipartFile file = imageFile("proof.txt", "text/plain");

        assertThatThrownBy(() -> storageService.storeOrderPhoto(file, "proof"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only JPG, PNG, or WEBP images are allowed");
    }

    private MockMultipartFile imageFile(String filename, String contentType) {
        return new MockMultipartFile("file", filename, contentType, "image-bytes".getBytes());
    }
}
