package org.example.controller;

import org.example.model.entity.Product;
import org.example.model.entity.User;
import org.example.model.entity.Warehouse;
import org.example.model.enums.Role;
import org.example.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Service testlerinde kapsanmayan Controller-katmanı mantığını test eder:
 * 1. UI'dan gelen miktar String'inin int parse işlemi ve
 *    NumberFormatException → IllegalArgumentException dönüşümü.
 * 2. Geçersiz işlem tipi String'inin (TransactionType.valueOf başarısız) → IllegalArgumentException dönüşümü.
 */
@DisplayName("TransactionController Katman Testleri")
class TransactionControllerTest {

    private final Product   dummyProduct   = new Product.ProductBuilder("SKU-1", "Ürün").quantityInStock(50).build();
    private final Warehouse dummyWarehouse = new Warehouse(1, "Merkez Depo", "Adres");
    private final User      dummyUser      = new User(1, "admin", "hash", "a@b.com", Role.ADMIN, true);

    // ── processTransaction ────────────────────────────────────────────────────

    @Test
    @DisplayName("processTransaction: sayısal olmayan miktar metni girildiğinde IllegalArgumentException fırlatmalı")
    void processTransaction_nonNumericQuantity_throwsException() {
        try (MockedConstruction<InventoryService> ignored = mockConstruction(InventoryService.class)) {
            TransactionController controller = new TransactionController();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> controller.processTransaction(
                            dummyProduct, dummyWarehouse, dummyUser, "IN", "abc", "not"));

            assertEquals("Miktar alanına sadece tam sayı girilmelidir.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("processTransaction: geçersiz işlem tipi girildiğinde IllegalArgumentException fırlatmalı")
    void processTransaction_invalidTransactionType_throwsException() {
        try (MockedConstruction<InventoryService> ignored = mockConstruction(InventoryService.class)) {
            TransactionController controller = new TransactionController();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> controller.processTransaction(
                            dummyProduct, dummyWarehouse, dummyUser, "GECERSIZ_TIP", "10", "not"));

            assertEquals("Hatalı işlem tipi seçildi.", ex.getMessage());
        }
    }
}
