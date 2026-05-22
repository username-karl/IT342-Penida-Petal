package com.petal.service;

import com.petal.dto.FloristResponse;
import com.petal.entity.Florist;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.FloristRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FloristServiceTest {

    @TempDir
    private Path uploadDir;

    private final FloristRepository floristRepository = mock(FloristRepository.class);

    @Test
    void floristCanUploadOwnLogo() {
        LocalStorageService storageService = new LocalStorageService(uploadDir.toString());
        FloristService floristService = new FloristService(floristRepository, storageService);
        User seller = seller();
        Florist florist = Florist.builder()
                .id(9L)
                .user(seller)
                .storeName("Karl's Studio")
                .bio("Preserved arrangements.")
                .build();
        MockMultipartFile file = imageFile("logo.png", "image/png");

        when(floristRepository.findByUser(seller)).thenReturn(Optional.of(florist));
        when(floristRepository.save(florist)).thenReturn(florist);

        FloristResponse response = floristService.uploadProfileImage(seller, file);

        assertThat(response.getLogoUrl()).startsWith("/uploads/florist-logos/logo-").endsWith(".png");
        assertThat(florist.getLogoUrl()).isEqualTo(response.getLogoUrl());
        verify(floristRepository).save(florist);
    }

    @Test
    void buyerCannotUploadLogo() {
        FloristService floristService = new FloristService(floristRepository, new LocalStorageService(uploadDir.toString()));
        User buyer = User.builder().id(2L).name("Buyer").role("ROLE_BUYER").build();

        assertThatThrownBy(() -> floristService.uploadProfileImage(buyer, imageFile("logo.jpg", "image/jpeg")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Florist access is required");
    }

    @Test
    void invalidLogoTypeIsRejected() {
        FloristService floristService = new FloristService(floristRepository, new LocalStorageService(uploadDir.toString()));
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();

        when(floristRepository.findByUser(seller)).thenReturn(Optional.of(florist));

        assertThatThrownBy(() -> floristService.uploadProfileImage(seller, imageFile("note.txt", "text/plain")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only JPG, PNG, or WEBP images are allowed");
    }

    @Test
    void emptyLogoFileIsRejected() {
        FloristService floristService = new FloristService(floristRepository, new LocalStorageService(uploadDir.toString()));
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();

        when(floristRepository.findByUser(seller)).thenReturn(Optional.of(florist));

        MockMultipartFile emptyFile = new MockMultipartFile("file", "logo.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> floristService.uploadProfileImage(seller, emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Photo file is required");
    }

    private User seller() {
        return User.builder()
                .id(4L)
                .name("Karl")
                .email("karl@petal.test")
                .role("ROLE_FLORIST")
                .build();
    }

    private MockMultipartFile imageFile(String filename, String contentType) {
        return new MockMultipartFile("file", filename, contentType, "image-bytes".getBytes());
    }
}
