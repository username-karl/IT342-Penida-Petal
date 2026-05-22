package com.petal.service;

import com.petal.dto.ProductResponse;
import com.petal.entity.Florist;
import com.petal.entity.Product;
import com.petal.entity.User;
import com.petal.repository.FloristRepository;
import com.petal.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final FloristRepository floristRepository = mock(FloristRepository.class);
    private final FloristService floristService = mock(FloristService.class);
    private final ProductService productService = new ProductService(productRepository, floristService, floristRepository);

    @Test
    void productResponseUsesCurrentFloristLogoNameAndBio() {
        Product product = Product.builder()
                .id(7L)
                .name("Island Blush")
                .description("Pink preserved flowers")
                .price(new BigDecimal("2100.00"))
                .moodTags(List.of("romance"))
                .imageUrl("/images/island-blush.webp")
                .floristId(9L)
                .floristName("Old Studio Name")
                .floristLogoUrl("/uploads/florist-logos/old-logo.png")
                .inStock(true)
                .build();
        Florist florist = Florist.builder()
                .id(9L)
                .user(User.builder().id(4L).build())
                .storeName("Cebu Florist Studio")
                .bio("Preserved blooms arranged in Cebu.")
                .logoUrl("/uploads/florist-logos/current-logo.png")
                .build();

        when(floristRepository.findById(9L)).thenReturn(Optional.of(florist));

        ProductResponse response = productService.toResponse(product);

        assertThat(response.getFloristName()).isEqualTo("Cebu Florist Studio");
        assertThat(response.getFloristLogoUrl()).isEqualTo("/uploads/florist-logos/current-logo.png");
        assertThat(response.getFloristBio()).isEqualTo("Preserved blooms arranged in Cebu.");
    }
}
