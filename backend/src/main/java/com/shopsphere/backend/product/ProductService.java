package com.shopsphere.backend.product;

import com.shopsphere.backend.category.Category;
import com.shopsphere.backend.category.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "name",
            "price",
            "stockQuantity",
            "sku"
    );

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    public ProductService (ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    private ProductResponse mapToResponse(Product product) {

        ProductResponse productResponse = new ProductResponse();

        productResponse.setId(product.getId());
        productResponse.setName(product.getName());
        productResponse.setDescription(product.getDescription());
        productResponse.setSku(product.getSku());
        productResponse.setPrice(product.getPrice());
        productResponse.setStockQuantity(product.getStockQuantity());
        productResponse.setBrand(product.getBrand());
        productResponse.setImageUrl(product.getImageUrl());
        productResponse.setActive(product.getActive());
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        productResponse.setCategoryId(
                product.getCategory() != null ? product.getCategory().getId() : null
        );
        return productResponse;
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException(
                    "Minimum and maximum prices are required."
            );
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price."
            );
        }
    }

    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Page size must be greater than zero."
            );
        }

        if (size > 100){
            throw new IllegalArgumentException(
                    "Page size cannot exceed 100."
            );
        }
    }

    private void validateSortField(String sortBy) {
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy
            );
        }
    }

    private void validateSortDirection(String direction) {
        if (direction == null ||
                (!direction.equalsIgnoreCase("ASC") &&
                        !direction.equalsIgnoreCase("DESC"))) {
            throw new IllegalArgumentException(
                    "Invalid sort direction: " + direction
            );
        }
    }

    private void validateSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException(
                    "Search keyword cannot be empty."
            );
        }
    }

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = new Product();
        Category category = categoryRepository
                .findById(productRequest.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + productRequest.getCategoryId()
                        )
                );
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setSku(productRequest.getSku());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setBrand(productRequest.getBrand());
        product.setImageUrl(productRequest.getImageUrl());
        product.setActive(productRequest.getActive());
        product.setCategory(category);

        return mapToResponse(productRepository.save(product));
    }

    public Page<ProductResponse> getAllProducts(
            int page, int size, String sortBy, String direction) {

        validateSortField(sortBy);
        validateSortDirection(direction);
        validatePaginationParameters(page, size);
        return productRepository.findAll(
                PageRequest.of(
                        page, size, Sort.by(Sort.Direction.fromString(direction), sortBy)))
                .map(this::mapToResponse);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return mapToResponse(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequest updatedProduct) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        Category category = categoryRepository
                .findById(updatedProduct.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + updatedProduct.getCategoryId()
                        )
                );
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        existingProduct.setActive(updatedProduct.getActive());
        existingProduct.setCategory(category);

        return mapToResponse(productRepository.save(existingProduct));
    }

    public void deleteProductById(Long id) {
        productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found: " + id));

        productRepository.deleteById(id);
    }

    public List<ProductResponse> searchProducts(String keyword) {

        validateSearchKeyword(keyword);
        return productRepository
                .findByNameContainingIgnoreCase(keyword)
                .stream().map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponse> filterByPrice(BigDecimal minPrice, BigDecimal maxPrice) {

        validatePriceRange(minPrice, maxPrice);
        return productRepository
                .findByPriceBetween(minPrice, maxPrice)
                .stream().map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponse> searchAndFilterProducts(
            String keyword, BigDecimal minPrice, BigDecimal maxPrice) {

        validateSearchKeyword(keyword);
        validatePriceRange(minPrice, maxPrice);
        return productRepository
                .findByNameContainingIgnoreCaseAndPriceBetween(keyword, minPrice, maxPrice)
                .stream().map(this::mapToResponse)
                .toList();
    }
}
