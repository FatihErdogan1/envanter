package org.example.service;

import org.example.dao.impl.InventoryTransactionDAO;
import org.example.dao.impl.ProductDAO;
import org.example.model.entity.InventoryTransaction;
import org.example.model.entity.Product;
import org.example.model.entity.Warehouse;
import org.example.model.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("InventoryService İş Kuralları Testleri")
class InventoryServiceTest {

    private Product buildProduct(int stock) {
        return new Product.ProductBuilder("SKU-001", "Test Ürün")
                .quantityInStock(stock)
                .build();
    }

    private Warehouse buildWarehouse(int id, String name) {
        Warehouse w = new Warehouse();
        w.setId(id);
        w.setName(name);
        return w;
    }

    private InventoryTransaction buildTransaction(Product product, TransactionType type, int quantity) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setProduct(product);
        tx.setWarehouse(buildWarehouse(1, "Merkez Depo"));
        tx.setType(type);
        tx.setQuantity(quantity);
        return tx;
    }

    private InventoryTransaction buildTransferTransaction(Product product, int quantity,
                                                          Warehouse source, Warehouse destination) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setProduct(product);
        tx.setType(TransactionType.TRANSFER);
        tx.setQuantity(quantity);
        tx.setWarehouse(source);
        tx.setDestinationWarehouse(destination);
        return tx;
    }

    @Test
    @DisplayName("İşlem miktarı sıfır olduğunda IllegalArgumentException fırlatmalı")
    void processTransaction_zeroQuantity_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignoredTxDao = mockConstruction(InventoryTransactionDAO.class)) {

            InventoryService service = new InventoryService();
            InventoryTransaction tx = buildTransaction(buildProduct(50), TransactionType.IN, 0);

            assertThrows(IllegalArgumentException.class, () -> service.processTransaction(tx));
        }
    }

    @Test
    @DisplayName("İşlem miktarı negatif olduğunda IllegalArgumentException fırlatmalı")
    void processTransaction_negativeQuantity_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignoredTxDao = mockConstruction(InventoryTransactionDAO.class)) {

            InventoryService service = new InventoryService();
            InventoryTransaction tx = buildTransaction(buildProduct(50), TransactionType.IN, -5);

            assertThrows(IllegalArgumentException.class, () -> service.processTransaction(tx));
        }
    }

    @Test
    @DisplayName("IN işlemi stok miktarını artırmalı ve her iki DAO çağrılmalı")
    void processTransaction_inType_increasesStock() {
        try (MockedConstruction<ProductDAO> mockedProdDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.update(any())).thenReturn(true));
             MockedConstruction<InventoryTransactionDAO> mockedTxDao = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.insert(any())).thenReturn(true))) {

            InventoryService service = new InventoryService();
            Product product = buildProduct(30);
            InventoryTransaction tx = buildTransaction(product, TransactionType.IN, 20);

            boolean result = service.processTransaction(tx);

            assertTrue(result);
            assertEquals(50, product.getQuantityInStock()); // 30 + 20
            verify(mockedTxDao.constructed().get(0), times(1)).insert(tx);
            verify(mockedProdDao.constructed().get(0), times(1)).update(product);
        }
    }

    @Test
    @DisplayName("OUT işlemi yeterli stok varken stok miktarını düşürmeli")
    void processTransaction_outType_sufficientStock_decreasesStock() {
        try (MockedConstruction<ProductDAO> mockedProdDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.update(any())).thenReturn(true));
             MockedConstruction<InventoryTransactionDAO> mockedTxDao = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.insert(any())).thenReturn(true))) {

            InventoryService service = new InventoryService();
            Product product = buildProduct(50);
            InventoryTransaction tx = buildTransaction(product, TransactionType.OUT, 15);

            boolean result = service.processTransaction(tx);

            assertTrue(result);
            assertEquals(35, product.getQuantityInStock()); // 50 - 15
            verify(mockedTxDao.constructed().get(0), times(1)).insert(tx);
            verify(mockedProdDao.constructed().get(0), times(1)).update(product);
        }
    }

    @Test
    @DisplayName("OUT işlemi depoda yeterli stok yokken IllegalArgumentException fırlatmalı")
    void processTransaction_outType_insufficientStock_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignoredTxDao = mockConstruction(InventoryTransactionDAO.class)) {

            InventoryService service = new InventoryService();
            Product product = buildProduct(10);
            InventoryTransaction tx = buildTransaction(product, TransactionType.OUT, 25);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.processTransaction(tx));
            assertTrue(ex.getMessage().contains("10")); // Mevcut stok mesajda yer almalı
        }
    }

    @Test
    @DisplayName("OUT işlemi tam stok miktarıyla başarılı olmalı (sınır durumu)")
    void processTransaction_outType_exactStock_succeeds() {
        try (MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.update(any())).thenReturn(true));
             MockedConstruction<InventoryTransactionDAO> ignoredTxDao = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.insert(any())).thenReturn(true))) {

            InventoryService service = new InventoryService();
            Product product = buildProduct(20);
            InventoryTransaction tx = buildTransaction(product, TransactionType.OUT, 20);

            boolean result = service.processTransaction(tx);

            assertTrue(result);
            assertEquals(0, product.getQuantityInStock()); // 20 - 20 = 0
        }
    }

    @Test
    @DisplayName("TRANSFER işlemi yeterli stok ve farklı depolarla başarılı olmalı, global stok değişmemeli")
    void processTransaction_transferType_success_stockUnchanged() {
        try (MockedConstruction<ProductDAO> mockedProdDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.update(any())).thenReturn(true));
             MockedConstruction<InventoryTransactionDAO> mockedTxDao = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.insert(any())).thenReturn(true))) {

            InventoryService service = new InventoryService();
            Product product = buildProduct(100);
            Warehouse src  = buildWarehouse(1, "Merkez Depo");
            Warehouse dest = buildWarehouse(2, "Şube-1");
            InventoryTransaction tx = buildTransferTransaction(product, 30, src, dest);

            boolean result = service.processTransaction(tx);

            assertTrue(result);
            assertEquals(100, product.getQuantityInStock()); // Transfer: global stok değişmez
            verify(mockedTxDao.constructed().get(0), times(1)).insert(tx);
            verify(mockedProdDao.constructed().get(0), never()).update(any()); // stok değişmez → update yok
        }
    }

    @Test
    @DisplayName("TRANSFER işleminde hedef depo null ise IllegalArgumentException fırlatmalı")
    void processTransaction_transferType_nullDestination_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignoredTxDao = mockConstruction(InventoryTransactionDAO.class)) {

            InventoryService service = new InventoryService();
            InventoryTransaction tx = buildTransferTransaction(buildProduct(50), 10,
                    buildWarehouse(1, "Merkez"), null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.processTransaction(tx));
            assertTrue(ex.getMessage().contains("hedef depo"));
        }
    }

    @Test
    @DisplayName("TRANSFER işleminde kaynak ve hedef depo aynıysa IllegalArgumentException fırlatmalı")
    void processTransaction_transferType_sameWarehouse_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignoredTxDao = mockConstruction(InventoryTransactionDAO.class)) {

            InventoryService service = new InventoryService();
            Warehouse same = buildWarehouse(1, "Merkez Depo");
            InventoryTransaction tx = buildTransferTransaction(buildProduct(50), 10, same, same);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.processTransaction(tx));
            assertTrue(ex.getMessage().contains("aynı olamaz"));
        }
    }

    @Test
    @DisplayName("TRANSFER işleminde stok yetersizse IllegalArgumentException fırlatmalı")
    void processTransaction_transferType_insufficientStock_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredProdDao = mockConstruction(ProductDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignoredTxDao = mockConstruction(InventoryTransactionDAO.class)) {

            InventoryService service = new InventoryService();
            Product product = buildProduct(5);
            InventoryTransaction tx = buildTransferTransaction(product, 20,
                    buildWarehouse(1, "Merkez"), buildWarehouse(2, "Şube-1"));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.processTransaction(tx));
            assertTrue(ex.getMessage().contains("5")); // Mevcut stok mesajda yer almalı
        }
    }

    @Test
    @DisplayName("TransactionDAO insert başarısız olursa productDAO update çağrılmamalı")
    void processTransaction_transactionSaveFails_productNotUpdated() {
        try (MockedConstruction<ProductDAO> mockedProdDao = mockConstruction(ProductDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignoredTxDao = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.insert(any())).thenReturn(false))) {

            InventoryService service = new InventoryService();
            Product product = buildProduct(50);
            InventoryTransaction tx = buildTransaction(product, TransactionType.IN, 10);

            boolean result = service.processTransaction(tx);

            assertFalse(result);
            verify(mockedProdDao.constructed().get(0), never()).update(any());
        }
    }
}
