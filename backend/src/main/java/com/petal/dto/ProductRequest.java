package com.petal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be 1000 characters or fewer")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotEmpty(message = "At least one mood tag is required")
    private List<String> moodTags;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private String floristName;
    private String floristLogoUrl;

    @Builder.Default
    private boolean inStock = true;
}
