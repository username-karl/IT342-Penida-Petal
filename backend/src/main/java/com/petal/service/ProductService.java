package com.petal.service;

import com.petal.dto.ProductRequest;
import com.petal.dto.ProductResponse;
import com.petal.entity.Florist;
import com.petal.entity.Mood;
import com.petal.entity.Product;
import com.petal.entity.User;
import com.petal.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FloristService floristService;

    /**
     * Get all products, optionally filtered by mood tag.
     */
    public List<ProductResponse> getAllProducts(String mood) {
        List<Product> products;

        if (mood != null && !mood.isBlank()) {
            try {
                Mood moodEnum = Mood.valueOf(mood.toUpperCase());
                products = productRepository.findByMoodTag(moodEnum);
            } catch (IllegalArgumentException e) {
                products = productRepository.findByInStockTrue();
            }
        } else {
            products = productRepository.findAll();
        }

        return products.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Get a single product by ID.
     */
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    /**
     * Get all products for the current florist.
     */
    public List<ProductResponse> getMyProducts(User user) {
        Florist florist = floristService.getOrCreateFlorist(user);
        List<Product> products = productRepository.findByFlorist(florist);
        return products.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Create a new product for the current florist.
     */
    public ProductResponse createProduct(User user, ProductRequest request) {
        Florist florist = floristService.getOrCreateFlorist(user);

        Set<Mood> moodTags = request.getMoodTags().stream()
                .map(tag -> Mood.valueOf(tag.toUpperCase()))
                .collect(Collectors.toSet());

        Product product = Product.builder()
                .florist(florist)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .moodTags(moodTags)
                .inStock(true)
                .build();

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    /**
     * Update an existing product owned by the current florist.
     */
    public ProductResponse updateProduct(User user, Long productId, ProductRequest request) {
        Florist florist = floristService.getOrCreateFlorist(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Verify ownership
        if (!product.getFlorist().getId().equals(florist.getId())) {
            throw new RuntimeException("You do not own this product");
        }

        Set<Mood> moodTags = request.getMoodTags().stream()
                .map(tag -> Mood.valueOf(tag.toUpperCase()))
                .collect(Collectors.toSet());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setMoodTags(moodTags);

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    /**
     * Toggle the stock status of a product.
     */
    public ProductResponse toggleStock(User user, Long productId, boolean inStock) {
        Florist florist = floristService.getOrCreateFlorist(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (!product.getFlorist().getId().equals(florist.getId())) {
            throw new RuntimeException("You do not own this product");
        }

        product.setInStock(inStock);
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    /**
     * Delete a product owned by the current florist.
     */
    public void deleteProduct(User user, Long productId) {
        Florist florist = floristService.getOrCreateFlorist(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (!product.getFlorist().getId().equals(florist.getId())) {
            throw new RuntimeException("You do not own this product");
        }

        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        Set<String> tags = product.getMoodTags().stream()
                .map(Mood::name)
                .collect(Collectors.toSet());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .inStock(product.isInStock())
                .moodTags(tags)
                .floristId(product.getFlorist().getId())
                .floristStoreName(product.getFlorist().getStoreName())
                .floristLogoUrl(product.getFlorist().getLogoUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
