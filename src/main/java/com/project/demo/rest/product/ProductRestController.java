package com.project.demo.rest.product;

import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductRestController {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    public  ProductRestController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }


    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }


    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                Category category = categoryRepository.findById(product.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Categoría no válida"));
                product.setCategory(category);
            }
            product.setId(null);
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (product.getPrice() == null || product.getPrice() < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (product.getStock() == null || product.getStock() < 0) {
                return ResponseEntity.badRequest().build();
            }
            Product savedProduct = productRepository.save(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
                Category category = categoryRepository.findById(productDetails.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Categoría no válida"));
                product.setCategory(category);
            }
            if (productDetails.getName() == null || productDetails.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (productDetails.getPrice() == null || productDetails.getPrice() < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (productDetails.getStock() == null || productDetails.getStock() < 0) {
                return ResponseEntity.badRequest().build();
            }
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setStock(productDetails.getStock());
            try {
                Product updatedProduct = productRepository.save(product);
                return ResponseEntity.ok(updatedProduct);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Product> patchProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (productDetails.getName() != null) {
                if (productDetails.getName().trim().isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
                product.setName(productDetails.getName());
            }
            if (productDetails.getDescription() != null) {
                product.setDescription(productDetails.getDescription());
            }
            if (productDetails.getPrice() != null) {
                if (productDetails.getPrice() < 0) {
                    return ResponseEntity.badRequest().build();
                }
                product.setPrice(productDetails.getPrice());
            }
            if (productDetails.getStock() != null) {
                if (productDetails.getStock() < 0) {
                    return ResponseEntity.badRequest().build();
                }
                product.setStock(productDetails.getStock());
            }
            if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
                Category category = categoryRepository.findById(productDetails.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Categoría no válida"));
                product.setCategory(category);
            }
            try {
                Product updatedProduct = productRepository.save(product);
                return ResponseEntity.ok(updatedProduct);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (stock < 0) {
                return ResponseEntity.badRequest().build();
            }
            product.setStock(stock);
            try {
                Product updatedProduct = productRepository.save(product);
                return ResponseEntity.ok(updatedProduct);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
            try {
                productRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
    }
}