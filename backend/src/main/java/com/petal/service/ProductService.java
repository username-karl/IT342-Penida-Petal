package com.petal.service;

import com.petal.dto.ProductRequest;
import com.petal.dto.ProductResponse;
import com.petal.entity.Florist;
import com.petal.entity.Product;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FloristService floristService;

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

    public List<ProductResponse> getSellerProducts(User seller) {
        requireFlorist(seller);

        Florist florist = floristService.getOrCreateForUser(seller);

        return productRepository.findByFloristIdOrderByIdDesc(florist.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse createSellerProduct(User seller, ProductRequest request) {
        requireFlorist(seller);

        Florist florist = floristService.getOrCreateForUser(seller);

        Product product = Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .price(request.getPrice())
                .moodTags(normalizeMoodTags(request.getMoodTags()))
                .imageUrl(request.getImageUrl().trim())
                .floristId(florist.getId())
                .floristName(resolveFloristName(florist, request.getFloristName()))
                .floristLogoUrl(request.getFloristLogoUrl())
                .inStock(request.isInStock())
                .build();

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateSellerProduct(User seller, Long productId, ProductRequest request) {
        requireFlorist(seller);
        Florist florist = floristService.getOrCreateForUser(seller);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        requireProductOwner(florist, product);

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.getMoodTags().clear();
        product.getMoodTags().addAll(normalizeMoodTags(request.getMoodTags()));
        product.setImageUrl(request.getImageUrl().trim());
        product.setFloristName(resolveFloristName(florist, request.getFloristName()));
        product.setFloristLogoUrl(request.getFloristLogoUrl());
        product.setInStock(request.isInStock());

        return toResponse(productRepository.save(product));
    }

    public void deleteSellerProduct(User seller, Long productId) {
        requireFlorist(seller);
        Florist florist = floristService.getOrCreateForUser(seller);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        requireProductOwner(florist, product);

        productRepository.delete(product);
    }

    public ProductResponse toResponse(Product product) {
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

    private void requireFlorist(User user) {
        if (user == null || !"ROLE_FLORIST".equals(user.getRole())) {
            throw new ForbiddenException("Florist access is required");
        }
    }

    private void requireProductOwner(Florist florist, Product product) {
        if (!florist.getId().equals(product.getFloristId())) {
            throw new ForbiddenException("You can only manage your own products");
        }
    }

    private List<String> normalizeMoodTags(List<String> moodTags) {
        return moodTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase())
                .distinct()
                .toList();
    }

    private String resolveFloristName(Florist florist, String requestedName) {
        if (requestedName != null && !requestedName.isBlank()) {
            return requestedName.trim();
        }
        if (florist.getStoreName() != null && !florist.getStoreName().isBlank()) {
            return florist.getStoreName();
        }
        return "Local Petal Florist";
    }
}
