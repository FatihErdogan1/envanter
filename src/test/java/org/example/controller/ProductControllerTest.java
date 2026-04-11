package org.example.controller;

import org.example.model.entity.Category;
import org.example.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Service testlerinde kapsanmayan Controller-katmanı mantığını test eder:
 * UI'dan gelen ham String input'larının (fiyat, miktar) sayısal parse işlemi
 * ve NumberFormatException → IllegalArgumentException dönüşümü.
 */
@DisplayName("ProductController Katman Testleri")
class ProductControllerTest {

    private final Category dummyCategory = new Category(1, "Elektronik", "");

    // ── addProduct ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addProduct: sayısal olmayan fiyat metni girildiğinde IllegalArgumentException fırlatmalı")
    void addProduct_nonNumericPrice_throwsException() {
        try (MockedConstruction<ProductService> ignored = mockConstruction(ProductService.class)) {
            ProductController controller = new ProductController();

            assertThrows(IllegalArgumentException.class,
                    () -> controller.addProduct("SKU-1", "Laptop", "abc", "10", dummyCategory));
        }
    }

    @Test
    @DisplayName("addProduct: sayısal olmayan miktar metni girildiğinde IllegalArgumentException fırlatmalı")
    void addProduct_nonNumericQuantity_throwsException() {
        try (MockedConstruction<ProductService> ignored = mockConstruction(ProductService.class)) {
            ProductController controller = new ProductController();

            assertThrows(IllegalArgumentException.class,
                    () -> controller.addProduct("SKU-1", "Laptop", "999.0", "abc", dummyCategory));
        }
    }

    // ── updateProduct ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProduct: sayısal olmayan fiyat metni girildiğinde IllegalArgumentException fırlatmalı")
    void updateProduct_nonNumericPrice_throwsException() {
        try (MockedConstruction<ProductService> ignored = mockConstruction(ProductService.class)) {
            ProductController controller = new ProductController();

            assertThrows(IllegalArgumentException.class,
                    () -> controller.updateProduct(1, "SKU-1", "Laptop", "abc", "10", dummyCategory));
        }
    }

    @Test
    @DisplayName("updateProduct: sayısal olmayan miktar metni girildiğinde IllegalArgumentException fırlatmalı")
    void updateProduct_nonNumericQuantity_throwsException() {
        try (MockedConstruction<ProductService> ignored = mockConstruction(ProductService.class)) {
            ProductController controller = new ProductController();

            assertThrows(IllegalArgumentException.class,
                    () -> controller.updateProduct(1, "SKU-1", "Laptop", "999.0", "abc", dummyCategory));
        }
    }
}
