package com.petal.controller;

import com.petal.dto.*;
import com.petal.entity.User;
import com.petal.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products?mood={mood}
     * Get all products with optional mood filter.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String mood) {
        List<ProductResponse> products = productService.getAllProducts(mood);
        return ResponseEntity.ok(ApiResponse.<List<ProductResponse>>builder()
                .success(true)
                .message("Products fetched successfully")
                .data(products)
                .build());
    }

    /**
     * GET /api/products/{id}
     * Get a single product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        try {
            ProductResponse product = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                    .success(true)
                    .message("Product fetched successfully")
                    .data(product)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ProductResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * GET /api/products/mine
     * Get all products for the current florist.
     */
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyProducts(
            @AuthenticationPrincipal User user) {
        List<ProductResponse> products = productService.getMyProducts(user);
        return ResponseEntity.ok(ApiResponse.<List<ProductResponse>>builder()
                .success(true)
                .message("Your products fetched successfully")
                .data(products)
                .build());
    }

    /**
     * POST /api/products
     * Create a new product (Florist only).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse product = productService.createProduct(user, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<ProductResponse>builder()
                            .success(true)
                            .message("Product created successfully")
                            .data(product)
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ProductResponse>builder()
                            .success(false)
                            .message("Invalid mood tag: " + e.getMessage())
                            .build());
        }
    }

    /**
     * PUT /api/products/{id}
     * Update an existing product (Florist only, must own the product).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse product = productService.updateProduct(user, id, request);
            return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                    .success(true)
                    .message("Product updated successfully")
                    .data(product)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ProductResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * PATCH /api/products/{id}/stock
     * Toggle product stock status (Florist only).
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleStock(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody StockUpdateRequest request) {
        try {
            ProductResponse product = productService.toggleStock(user, id, request.isInStock());
            return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                    .success(true)
                    .message("Stock status updated successfully")
                    .data(product)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ProductResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * DELETE /api/products/{id}
     * Delete a product (Florist only, must own the product).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        try {
            productService.deleteProduct(user, id);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Product deleted successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
}
