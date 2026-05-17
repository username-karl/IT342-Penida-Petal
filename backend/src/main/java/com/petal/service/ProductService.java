package com.petal.service;

import com.petal.dto.ProductResponse;
import com.petal.entity.Product;
import com.petal.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> getProducts(String mood) {
        List<Product> products = mood == null || mood.isBlank()
                ? productRepository.findAll()
                : productRepository.findByMood(mood.trim());

        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .moodTags(product.getMoodTags())
                .imageUrl(product.getImageUrl())
                .floristId(product.getFloristId())
                .floristName(product.getFloristName())
                .floristLogoUrl(product.getFloristLogoUrl())
                .inStock(product.isInStock())
                .build();
    }
}
