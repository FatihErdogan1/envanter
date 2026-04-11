package org.example.service;

import org.example.dao.impl.AssetDAO;
import org.example.dao.impl.SupplierDAO;
import org.example.model.entity.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@DisplayName("SupplierService İş Kuralları Testleri")
class SupplierServiceTest {

    // ── addSupplier ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Firma adı null olduğunda IllegalArgumentException fırlatmalı")
    void addSupplier_nullName_throwsException() {
        try (MockedConstruction<SupplierDAO> ignored1 = mockConstruction(SupplierDAO.class);
             MockedConstruction<AssetDAO>    ignored2 = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();
            Supplier supplier = new Supplier(0, null, "info@firma.com", "5551234567", "İstanbul");

            assertThrows(IllegalArgumentException.class, () -> service.addSupplier(supplier));
        }
    }

    @Test
    @DisplayName("Firma adı boş string olduğunda IllegalArgumentException fırlatmalı")
    void addSupplier_emptyName_throwsException() {
        try (MockedConstruction<SupplierDAO> ignored1 = mockConstruction(SupplierDAO.class);
             MockedConstruction<AssetDAO>    ignored2 = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();
            Supplier supplier = new Supplier(0, "   ", "info@firma.com", "5551234567", "İstanbul");

            assertThrows(IllegalArgumentException.class, () -> service.addSupplier(supplier));
        }
    }

    @Test
    @DisplayName("E-posta @ içermiyorsa IllegalArgumentException fırlatmalı")
    void addSupplier_emailMissingAt_throwsException() {
        try (MockedConstruction<SupplierDAO> ignored1 = mockConstruction(SupplierDAO.class);
             MockedConstruction<AssetDAO>    ignored2 = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();
            Supplier supplier = new Supplier(0, "Firma A", "infofirma.com", "5551234567", "İstanbul");

            assertThrows(IllegalArgumentException.class, () -> service.addSupplier(supplier));
        }
    }

    @Test
    @DisplayName("E-posta nokta içermiyorsa IllegalArgumentException fırlatmalı")
    void addSupplier_emailMissingDot_throwsException() {
        try (MockedConstruction<SupplierDAO> ignored1 = mockConstruction(SupplierDAO.class);
             MockedConstruction<AssetDAO>    ignored2 = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();
            Supplier supplier = new Supplier(0, "Firma A", "info@firmacom", "5551234567", "İstanbul");

            assertThrows(IllegalArgumentException.class, () -> service.addSupplier(supplier));
        }
    }

    @Test
    @DisplayName("E-posta null ise format kontrolü yapılmamalı (geçerli)")
    void addSupplier_nullEmail_isValid() {
        try (MockedConstruction<SupplierDAO> mockedDao = mockConstruction(SupplierDAO.class,
                (mock, ctx) -> when(mock.insert(any())).thenReturn(true));
             MockedConstruction<AssetDAO> ignored = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();
            Supplier supplier = new Supplier(0, "Firma A", null, "5551234567", "İstanbul");

            boolean result = service.addSupplier(supplier);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).insert(supplier);
        }
    }

    @Test
    @DisplayName("Telefon numarası 20 karakterden uzunsa IllegalArgumentException fırlatmalı")
    void addSupplier_phoneTooLong_throwsException() {
        try (MockedConstruction<SupplierDAO> ignored1 = mockConstruction(SupplierDAO.class);
             MockedConstruction<AssetDAO>    ignored2 = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();
            Supplier supplier = new Supplier(0, "Firma A", "info@firma.com", "1".repeat(21), "İstanbul");

            assertThrows(IllegalArgumentException.class, () -> service.addSupplier(supplier));
        }
    }

    @Test
    @DisplayName("Tam 20 karakterlik telefon numarası geçerlidir")
    void addSupplier_phoneExactly20Chars_isValid() {
        try (MockedConstruction<SupplierDAO> mockedDao = mockConstruction(SupplierDAO.class,
                (mock, ctx) -> when(mock.insert(any())).thenReturn(true));
             MockedConstruction<AssetDAO> ignored = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();
            Supplier supplier = new Supplier(0, "Firma A", "info@firma.com", "1".repeat(20), "İstanbul");

            boolean result = service.addSupplier(supplier);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).insert(supplier);
        }
    }

    @Test
    @DisplayName("Tüm kurallar sağlandığında DAO insert çağrılmalı")
    void addSupplier_validSupplier_callsDaoInsert() {
        try (MockedConstruction<SupplierDAO> mockedDao = mockConstruction(SupplierDAO.class,
                (mock, ctx) -> when(mock.insert(any())).thenReturn(true));
             MockedConstruction<AssetDAO> ignored = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();
            Supplier supplier = new Supplier(0, "Teknoloji A.Ş.", "info@teknoloji.com", "02121234567", "İstanbul");

            boolean result = service.addSupplier(supplier);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).insert(supplier);
        }
    }

    // ── getAllSuppliers / updateSupplier ──────────────────────────────────────

    @Test
    @DisplayName("getAllSuppliers çağrıldığında DAO getAll sonucu dönmeli")
    void getAllSuppliers_returnsList() {
        List<Supplier> mockList = Arrays.asList(
                new Supplier(1, "Firma A", "a@firma.com", "111", "Ankara"),
                new Supplier(2, "Firma B", "b@firma.com", "222", "İzmir")
        );

        try (MockedConstruction<SupplierDAO> mockedDao = mockConstruction(SupplierDAO.class,
                (mock, ctx) -> when(mock.getAll()).thenReturn(mockList));
             MockedConstruction<AssetDAO> ignored = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();

            List<Supplier> result = service.getAllSuppliers();

            assertEquals(2, result.size());
            verify(mockedDao.constructed().get(0), times(1)).getAll();
        }
    }

    @Test
    @DisplayName("updateSupplier çağrıldığında DAO update çağrılmalı ve sonucu dönmeli")
    void updateSupplier_callsDaoUpdate() {
        try (MockedConstruction<SupplierDAO> mockedDao = mockConstruction(SupplierDAO.class,
                (mock, ctx) -> when(mock.update(any())).thenReturn(true));
             MockedConstruction<AssetDAO> ignored = mockConstruction(AssetDAO.class)) {

            SupplierService service = new SupplierService();
            Supplier supplier = new Supplier(1, "Firma A Güncel", "yeni@firma.com", "5559876543", "Ankara");

            boolean result = service.updateSupplier(supplier);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).update(supplier);
        }
    }

    // ── deleteSupplier ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Bağlı demirbaş yoksa DAO delete çağrılmalı ve true dönmeli")
    void deleteSupplier_noLinkedAssets_callsDaoDelete() {
        try (MockedConstruction<SupplierDAO> mockedSupplier = mockConstruction(SupplierDAO.class,
                (mock, ctx) -> when(mock.delete(anyInt())).thenReturn(true));
             MockedConstruction<AssetDAO> mockedAsset = mockConstruction(AssetDAO.class,
                (mock, ctx) -> when(mock.countBySupplierId(anyInt())).thenReturn(0))) {

            SupplierService service = new SupplierService();

            boolean result = service.deleteSupplier(1);

            assertTrue(result);
            verify(mockedAsset.constructed().get(0), times(1)).countBySupplierId(1);
            verify(mockedSupplier.constructed().get(0), times(1)).delete(1);
        }
    }

    @Test
    @DisplayName("Bağlı demirbaş varsa IllegalStateException fırlatmalı, DAO delete çağrılmamalı")
    void deleteSupplier_withLinkedAssets_throwsException() {
        try (MockedConstruction<SupplierDAO> mockedSupplier = mockConstruction(SupplierDAO.class);
             MockedConstruction<AssetDAO> mockedAsset = mockConstruction(AssetDAO.class,
                (mock, ctx) -> when(mock.countBySupplierId(anyInt())).thenReturn(3))) {

            SupplierService service = new SupplierService();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.deleteSupplier(1));

            assertTrue(ex.getMessage().contains("3"));
            verify(mockedSupplier.constructed().get(0), never()).delete(anyInt());
        }
    }
}
