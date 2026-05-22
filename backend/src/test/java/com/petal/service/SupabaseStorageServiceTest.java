package com.petal.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SupabaseStorageServiceTest {

    @Test
    void storesPrivateOrderPhotoAsObjectReferenceAndResolvesSignedUrl() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        SupabaseStorageService storageService = new SupabaseStorageService(
                builder,
                new StorageProperties(
                        "supabase",
                        "https://project-ref.supabase.co",
                        "service-role-secret",
                        "florist-logos",
                        "order-photos",
                        300,
                        "uploads"));
        MockMultipartFile file = new MockMultipartFile("file", "proof.jpg", "image/jpeg", "image-bytes".getBytes());

        server.expect(once(), requestTo(org.hamcrest.Matchers.matchesPattern(
                        "https://project-ref\\.supabase\\.co/storage/v1/object/order-photos/proof-[a-f0-9\\-]+\\.jpg")))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("Authorization", "Bearer service-role-secret"))
                .andExpect(header("apikey", "service-role-secret"))
                .andRespond(withSuccess("{\"Key\":\"order-photos/proof.jpg\"}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(org.hamcrest.Matchers.matchesPattern(
                        "https://project-ref\\.supabase\\.co/storage/v1/object/sign/order-photos/proof-[a-f0-9\\-]+\\.jpg")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer service-role-secret"))
                .andRespond(withSuccess("{\"signedURL\":\"/storage/v1/object/sign/order-photos/proof-fixed.jpg?token=signed\"}", MediaType.APPLICATION_JSON));

        String storedPath = storageService.storeOrderPhoto(file, "proof");

        assertThat(storedPath).startsWith("supabase://order-photos/proof-").endsWith(".jpg");

        assertThat(storageService.resolveUrl(storedPath))
                .isEqualTo("https://project-ref.supabase.co/storage/v1/object/sign/order-photos/proof-fixed.jpg?token=signed");
        server.verify();
    }

    @Test
    void storesFloristLogoAsPublicUrl() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        SupabaseStorageService storageService = new SupabaseStorageService(
                builder,
                new StorageProperties(
                        "supabase",
                        "https://project-ref.supabase.co",
                        "service-role-secret",
                        "florist-logos",
                        "order-photos",
                        300,
                        "uploads"));
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "image-bytes".getBytes());

        server.expect(once(), requestTo(org.hamcrest.Matchers.matchesPattern(
                        "https://project-ref\\.supabase\\.co/storage/v1/object/florist-logos/logo-[a-f0-9\\-]+\\.png")))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("{\"Key\":\"florist-logos/logo.png\"}", MediaType.APPLICATION_JSON));

        String publicUrl = storageService.storeFloristLogo(file);

        assertThat(publicUrl).startsWith("https://project-ref.supabase.co/storage/v1/object/public/florist-logos/logo-")
                .endsWith(".png");
        server.verify();
    }
}
