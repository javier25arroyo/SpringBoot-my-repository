package com.project.demo.logic.entity.product;

import com.project.demo.logic.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar productos por categoría
    List<Product> findByCategory(Category category);

    // Buscar productos por nombre (búsqueda parcial, case insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Buscar productos por rango de precio
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // Buscar productos con stock mayor a cierta cantidad
    List<Product> findByStockGreaterThan(Integer stock);

    // Buscar productos sin stock
    List<Product> findByStockEquals(Integer stock);

    // Eliminar productos por categoría
    void deleteByCategory(Category category);

    // Contar productos por categoría
    long countByCategory(Category category);

    // Buscar productos por categoría y con stock disponible
    List<Product> findByCategoryAndStockGreaterThan(Category category, Integer stock);
}