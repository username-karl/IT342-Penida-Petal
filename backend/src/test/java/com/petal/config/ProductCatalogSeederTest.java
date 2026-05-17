package com.petal.config;

import com.petal.entity.Product;
import com.petal.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductCatalogSeederTest {

    @Test
    void seedsStarterCatalogWhenNoProductsExist() throws Exception {
        ProductRepository productRepository = mock(ProductRepository.class);
        when(productRepository.existsByName(anyString())).thenReturn(false);

        ProductCatalogSeeder seeder = new ProductCatalogSeeder(productRepository);

        seeder.run();

        verify(productRepository).saveAll(anyList());
    }

    @Test
    void doesNotSeedWhenProductsAlreadyExist() throws Exception {
        ProductRepository productRepository = mock(ProductRepository.class);
        when(productRepository.existsByName(anyString())).thenReturn(true);

        ProductCatalogSeeder seeder = new ProductCatalogSeeder(productRepository);

        seeder.run();

        verify(productRepository, never()).saveAll(anyList());
    }

    @Test
    void seedsStarterCatalogWhenOnlyLegacyProductsExist() throws Exception {
        ProductRepository productRepository = mock(ProductRepository.class);
        when(productRepository.existsByName(anyString())).thenReturn(false);

        ProductCatalogSeeder seeder = new ProductCatalogSeeder(productRepository);

        seeder.run();

        verify(productRepository).saveAll(anyList());
    }

    @Test
    void starterCatalogIncludesMoodTaggedProducts() {
        ProductRepository productRepository = mock(ProductRepository.class);

        List<Product> products = new ProductCatalogSeeder(productRepository).starterProducts();

        assertThat(products).hasSize(6);
        assertThat(products)
                .anySatisfy(product -> {
                    assertThat(product.getName()).isEqualTo("Wild Pampas");
                    assertThat(product.getMoodTags()).contains("friendship", "just because");
                })
                .anySatisfy(product -> {
                    assertThat(product.getName()).isEqualTo("The Aurora");
                    assertThat(product.getMoodTags()).contains("romance", "celebration");
                });
    }
}
