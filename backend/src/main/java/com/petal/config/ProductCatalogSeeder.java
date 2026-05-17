package com.petal.config;

import com.petal.entity.Product;
import com.petal.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductCatalogSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        List<Product> missingProducts = starterProducts().stream()
                .filter(product -> !productRepository.existsByName(product.getName()))
                .toList();

        if (!missingProducts.isEmpty()) {
            productRepository.saveAll(missingProducts);
        }
    }

    List<Product> starterProducts() {
        return List.of(
                Product.builder()
                        .name("Wild Pampas")
                        .description("Dried reed grass arrangement with warm natural texture.")
                        .price(new BigDecimal("45.00"))
                        .moodTags(List.of("friendship", "just because"))
                        .imageUrl("/images/product_pampas_1771726515735.png")
                        .floristId(1L)
                        .floristName("Atelier Vert")
                        .inStock(true)
                        .build(),
                Product.builder()
                        .name("Eucalyptus Cinerea")
                        .description("Preserved foliage with a cool botanical finish.")
                        .price(new BigDecimal("28.00"))
                        .moodTags(List.of("sympathy", "just because"))
                        .imageUrl("/images/product_eucalyptus_1771726530879.png")
                        .floristId(1L)
                        .floristName("Maison Fleuri")
                        .inStock(true)
                        .build(),
                Product.builder()
                        .name("Cotton Softness")
                        .description("Natural cotton stems arranged for a gentle, comforting gift.")
                        .price(new BigDecimal("32.00"))
                        .moodTags(List.of("apology", "sympathy"))
                        .imageUrl("/images/product_cotton_1771726545396.png")
                        .floristId(1L)
                        .floristName("Studio Petal")
                        .inStock(true)
                        .build(),
                Product.builder()
                        .name("Kanso Vase")
                        .description("Artisan ceramic vase for quiet interiors and lasting displays.")
                        .price(new BigDecimal("55.00"))
                        .moodTags(List.of("celebration", "just because"))
                        .imageUrl("/images/product_ceramic_vase_1771726567287.png")
                        .floristId(1L)
                        .floristName("Ceramics by Jo")
                        .inStock(true)
                        .build(),
                Product.builder()
                        .name("The Aurora")
                        .description("Hydrangea and immortelle arrangement with a romantic preserved finish.")
                        .price(new BigDecimal("49.00"))
                        .moodTags(List.of("romance", "celebration"))
                        .imageUrl("/images/product_aurora_hydrangea_1771726583839.png")
                        .floristId(1L)
                        .floristName("L'Herbier")
                        .inStock(true)
                        .build(),
                Product.builder()
                        .name("Winter Wreath")
                        .description("Pine and berry wreath for seasonal celebration and thoughtful gifting.")
                        .price(new BigDecimal("65.00"))
                        .moodTags(List.of("celebration", "friendship"))
                        .imageUrl("/images/product_winter_wreath_1771726603408.png")
                        .floristId(1L)
                        .floristName("Forest & Co.")
                        .inStock(true)
                        .build());
    }
}
