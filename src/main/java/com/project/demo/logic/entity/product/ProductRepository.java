package com.project.demo.logic.entity.product;

import com.project.demo.logic.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(Category category);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    List<Product> findByStockGreaterThan(Integer stock);

    List<Product> findByStockEquals(Integer stock);

    void deleteByCategory(Category category);

    long countByCategory(Category category);

    List<Product> findByCategoryAndStockGreaterThan(Category category, Integer stock);
}