package com.shopsphere.backend.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository <Product, Long> {

    public List<Product> findByNameContainingIgnoreCase(String keyword);

    public List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    public List<Product> findByNameContainingIgnoreCaseAndPriceBetween(
            String keyword, BigDecimal minPrice, BigDecimal maxPrice );

    public boolean existsByCategoryId(Long categoryId);

}
