package org.example.controller;

import org.example.model.entity.Product;
import org.example.model.entity.User;
import org.example.model.enums.Role;
import org.example.service.ProductService;
import org.example.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Service testlerinde kapsanmayan Controller-katmanı mantığını test eder:
 * getLowStockCount içindeki stream/filter iş mantığı (eşik: quantityInStock < 10).
 * getTotalProductCount ve getTotalUserCount delegasyon doğrulaması.
 */
@DisplayName("DashboardController Katman Testleri")
class DashboardControllerTest {

    // ── getLowStockCount ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getLowStockCount: stoğu 10'un altındaki ürünleri doğru saymalı")
    void getLowStockCount_mixedStock_returnsCorrectCount() {
        List<Product> products = Arrays.asList(
                new Product.ProductBuilder("SKU-1", "A").quantityInStock(5).build(),  // düşük stok
                new Product.ProductBuilder("SKU-2", "B").quantityInStock(15).build(), // yeterli
                new Product.ProductBuilder("SKU-3", "C").quantityInStock(9).build(),  // düşük stok (sınır altı)
                new Product.ProductBuilder("SKU-4", "D").quantityInStock(10).build()  // tam eşik — sayılmamalı
        );

        try (MockedConstruction<ProductService> mockedProd = mockConstruction(ProductService.class,
                (mock, ctx) -> when(mock.getAllProducts()).thenReturn(products));
             MockedConstruction<UserService> ignoredUser = mockConstruction(UserService.class)) {

            DashboardController controller = new DashboardController();

            assertEquals(2, controller.getLowStockCount());
        }
    }

    @Test
    @DisplayName("getLowStockCount: tüm ürünlerin stoğu yeterliyse 0 dönmeli")
    void getLowStockCount_allSufficientStock_returnsZero() {
        List<Product> products = Arrays.asList(
                new Product.ProductBuilder("SKU-1", "A").quantityInStock(10).build(),
                new Product.ProductBuilder("SKU-2", "B").quantityInStock(100).build()
        );

        try (MockedConstruction<ProductService> mockedProd = mockConstruction(ProductService.class,
                (mock, ctx) -> when(mock.getAllProducts()).thenReturn(products));
             MockedConstruction<UserService> ignoredUser = mockConstruction(UserService.class)) {

            DashboardController controller = new DashboardController();

            assertEquals(0, controller.getLowStockCount());
        }
    }

    @Test
    @DisplayName("getLowStockCount: ürün listesi boşsa 0 dönmeli")
    void getLowStockCount_emptyList_returnsZero() {
        try (MockedConstruction<ProductService> mockedProd = mockConstruction(ProductService.class,
                (mock, ctx) -> when(mock.getAllProducts()).thenReturn(Collections.emptyList()));
             MockedConstruction<UserService> ignoredUser = mockConstruction(UserService.class)) {

            DashboardController controller = new DashboardController();

            assertEquals(0, controller.getLowStockCount());
        }
    }

    // ── getTotalProductCount ──────────────────────────────────────────────────

    @Test
    @DisplayName("getTotalProductCount: ürün sayısını doğru döndürmeli")
    void getTotalProductCount_returnsCorrectSize() {
        List<Product> products = Arrays.asList(
                new Product.ProductBuilder("SKU-1", "A").build(),
                new Product.ProductBuilder("SKU-2", "B").build(),
                new Product.ProductBuilder("SKU-3", "C").build()
        );

        try (MockedConstruction<ProductService> mockedProd = mockConstruction(ProductService.class,
                (mock, ctx) -> when(mock.getAllProducts()).thenReturn(products));
             MockedConstruction<UserService> ignoredUser = mockConstruction(UserService.class)) {

            DashboardController controller = new DashboardController();

            assertEquals(3, controller.getTotalProductCount());
        }
    }

    // ── getTotalUserCount ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getTotalUserCount: kullanıcı sayısını doğru döndürmeli")
    void getTotalUserCount_returnsCorrectSize() {
        List<User> users = Arrays.asList(
                new User(1, "admin", "h", "a@a.com", Role.ADMIN, true),
                new User(2, "ali",   "h", "b@b.com", Role.STAFF, true)
        );

        try (MockedConstruction<ProductService> ignoredProd = mockConstruction(ProductService.class,
                (mock, ctx) -> when(mock.getAllProducts()).thenReturn(Collections.emptyList()));
             MockedConstruction<UserService> mockedUser = mockConstruction(UserService.class,
                     (mock, ctx) -> when(mock.getAllUsers()).thenReturn(users))) {

            DashboardController controller = new DashboardController();

            assertEquals(2, controller.getTotalUserCount());
        }
    }
}
