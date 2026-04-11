package org.example.service;

import org.example.dao.impl.InventoryTransactionDAO;
import org.example.dao.impl.ProductDAO;
import org.example.model.entity.Category;
import org.example.model.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@DisplayName("ProductService İş Kuralları Testleri")
class ProductServiceTest {

    /**
     * Testlerde kullanılacak örnek bir ürün nesnesi oluşturur.
     */
    private Product buildProduct(String sku, String name, Category category, double price, int qty) {
        return new Product.ProductBuilder(sku, name)
                .category(category)
                .price(price)
                .quantityInStock(qty)
                .build();
    }

    @Test
    @DisplayName("SKU null olduğunda IllegalArgumentException fırlatmalı")
    void addProduct_nullSku_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredDao = mockConstruction(ProductDAO.class)) {
            ProductService service = new ProductService();
            Product product = buildProduct(null, "Laptop", new Category(1, "Elektronik", ""), 999.0, 10);

            assertThrows(IllegalArgumentException.class, () -> service.addProduct(product));
        }
    }

    @Test
    @DisplayName("SKU boş string olduğunda IllegalArgumentException fırlatmalı")
    void addProduct_emptySku_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredDao = mockConstruction(ProductDAO.class)) {
            ProductService service = new ProductService();
            Product product = buildProduct("   ", "Laptop", new Category(1, "Elektronik", ""), 999.0, 10);

            assertThrows(IllegalArgumentException.class, () -> service.addProduct(product));
        }
    }

    @Test
    @DisplayName("Ürün adı boş olduğunda IllegalArgumentException fırlatmalı")
    void addProduct_emptyName_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredDao = mockConstruction(ProductDAO.class)) {
            ProductService service = new ProductService();
            Product product = buildProduct("SKU-001", "  ", new Category(1, "Elektronik", ""), 999.0, 10);

            assertThrows(IllegalArgumentException.class, () -> service.addProduct(product));
        }
    }

    @Test
    @DisplayName("Kategori null olduğunda IllegalArgumentException fırlatmalı")
    void addProduct_nullCategory_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredDao = mockConstruction(ProductDAO.class)) {
            ProductService service = new ProductService();
            Product product = buildProduct("SKU-001", "Laptop", null, 999.0, 10);

            assertThrows(IllegalArgumentException.class, () -> service.addProduct(product));
        }
    }

    @Test
    @DisplayName("Fiyat negatif olduğunda IllegalArgumentException fırlatmalı")
    void addProduct_negativePrice_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredDao = mockConstruction(ProductDAO.class)) {
            ProductService service = new ProductService();
            Product product = buildProduct("SKU-001", "Laptop", new Category(1, "Elektronik", ""), -1.0, 10);

            assertThrows(IllegalArgumentException.class, () -> service.addProduct(product));
        }
    }

    @Test
    @DisplayName("Başlangıç stoku negatif olduğunda IllegalArgumentException fırlatmalı")
    void addProduct_negativeStock_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredDao = mockConstruction(ProductDAO.class)) {
            ProductService service = new ProductService();
            Product product = buildProduct("SKU-001", "Laptop", new Category(1, "Elektronik", ""), 999.0, -1);

            assertThrows(IllegalArgumentException.class, () -> service.addProduct(product));
        }
    }

    @Test
    @DisplayName("Fiyat sıfır olduğunda geçerli kabul edilmeli")
    void addProduct_zeroPriceIsValid() {
        try (MockedConstruction<ProductDAO> mockedDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.insert(any())).thenReturn(true))) {

            ProductService service = new ProductService();
            Product product = buildProduct("SKU-001", "Promosyon Ürün", new Category(1, "Kampanya", ""), 0.0, 5);

            boolean result = service.addProduct(product);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).insert(product);
        }
    }

    @Test
    @DisplayName("Geçerli ürün eklendiğinde DAO insert çağrılmalı ve true dönmeli")
    void addProduct_validProduct_returnsTrueAndCallsDao() {
        try (MockedConstruction<ProductDAO> mockedDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.insert(any())).thenReturn(true))) {

            ProductService service = new ProductService();
            Product product = buildProduct("SKU-001", "Laptop", new Category(1, "Elektronik", ""), 15000.0, 50);

            boolean result = service.addProduct(product);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).insert(product);
        }
    }

    @Test
    @DisplayName("Güncelleme sırasında fiyat negatifse IllegalArgumentException fırlatmalı")
    void updateProduct_negativePrice_throwsException() {
        try (MockedConstruction<ProductDAO> ignoredDao = mockConstruction(ProductDAO.class)) {
            ProductService service = new ProductService();
            Product product = buildProduct("SKU-001", "Laptop", new Category(1, "Elektronik", ""), -50.0, 10);

            assertThrows(IllegalArgumentException.class, () -> service.updateProduct(product));
        }
    }

    @Test
    @DisplayName("Geçerli güncelleme isteği DAO update çağırmalı")
    void updateProduct_validProduct_callsDaoUpdate() {
        try (MockedConstruction<ProductDAO> mockedDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.update(any())).thenReturn(true))) {

            ProductService service = new ProductService();
            Product product = buildProduct("SKU-001", "Laptop", new Category(1, "Elektronik", ""), 999.0, 10);

            boolean result = service.updateProduct(product);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).update(product);
        }
    }

    @Test
    @DisplayName("getAllProducts çağrıldığında DAO getAll sonucu dönmeli")
    void getAllProducts_returnsList() {
        Product p1 = buildProduct("SKU-001", "Laptop", new Category(1, "Elektronik", ""), 999.0, 10);
        Product p2 = buildProduct("SKU-002", "Mouse", new Category(1, "Elektronik", ""), 99.0, 50);
        List<Product> mockList = Arrays.asList(p1, p2);

        try (MockedConstruction<ProductDAO> mockedDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.getAll()).thenReturn(mockList))) {

            ProductService service = new ProductService();

            List<Product> result = service.getAllProducts();

            assertEquals(2, result.size());
            verify(mockedDao.constructed().get(0), times(1)).getAll();
        }
    }

    @Test
    @DisplayName("deleteProduct: Stok hareketi olmayan ürün silinebilmeli")
    void deleteProduct_noTransactions_callsDaoDelete() {
        // İki farklı DAO'yu da mock'lamamız gerekiyor çünkü servis içinde ikisi de 'new' ile oluşturuluyor.
        try (MockedConstruction<ProductDAO> mockedProdDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.delete(1)).thenReturn(true));
             MockedConstruction<InventoryTransactionDAO> mockedTxDao = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.countByProductId(1)).thenReturn(0))) {

            ProductService service = new ProductService();
            boolean result = service.deleteProduct(1);

            assertTrue(result);
            // ProductDAO.delete çağrılmalı
            verify(mockedProdDao.constructed().get(0), times(1)).delete(1);
            // InventoryTransactionDAO.countByProductId çağrılmalı
            verify(mockedTxDao.constructed().get(0), times(1)).countByProductId(1);
        }
    }

    @Test
    @DisplayName("deleteProduct: Stok hareketi olan ürün silinemez -> IllegalStateException")
    void deleteProduct_hasTransactions_throwsException() {
        try (MockedConstruction<ProductDAO> mockedProdDao = mockConstruction(ProductDAO.class);
             MockedConstruction<InventoryTransactionDAO> mockedTxDao = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.countByProductId(1)).thenReturn(21))) {

            ProductService service = new ProductService();

            Exception exception = assertThrows(IllegalStateException.class, () -> {
                service.deleteProduct(1);
            });

            assertTrue(exception.getMessage().contains("stok hareketi kaydı bulunduğundan silinemez"));
            // Silme işlemi asla tetiklenmemeli
            verify(mockedProdDao.constructed().get(0), never()).delete(anyInt());
        }
    }

    @Test
    @DisplayName("getProductById çağrıldığında DAO getById sonucu dönmeli")
    void getProductById_returnsProduct() {
        Product mockProduct = buildProduct("SKU-001", "Laptop", new Category(1, "Elektronik", ""), 999.0, 10);

        try (MockedConstruction<ProductDAO> mockedDao = mockConstruction(ProductDAO.class,
                (mock, ctx) -> when(mock.getById(1)).thenReturn(mockProduct))) {

            ProductService service = new ProductService();

            Product result = service.getProductById(1);

            assertNotNull(result);
            assertEquals("SKU-001", result.getSku());
            verify(mockedDao.constructed().get(0), times(1)).getById(1);
        }
    }
}