package org.example.service;

import org.example.dao.impl.AssetDAO;
import org.example.dao.impl.InventoryTransactionDAO;
import org.example.dao.impl.WarehouseDAO;
import org.example.model.entity.Warehouse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@DisplayName("WarehouseService İş Kuralları Testleri")
class WarehouseServiceTest {

    @Test
    @DisplayName("Depo adı null olduğunda IllegalArgumentException fırlatmalı")
    void addWarehouse_nullName_throwsException() {
        try (MockedConstruction<WarehouseDAO> ignored1 = mockConstruction(WarehouseDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignored2 = mockConstruction(InventoryTransactionDAO.class);
             MockedConstruction<AssetDAO> ignored3 = mockConstruction(AssetDAO.class)) {

            WarehouseService service = new WarehouseService();
            assertThrows(IllegalArgumentException.class,
                    () -> service.addWarehouse(new Warehouse(0, null, "Atatürk Cad. No:1")));
        }
    }

    @Test
    @DisplayName("Depo adı boş string olduğunda IllegalArgumentException fırlatmalı")
    void addWarehouse_emptyName_throwsException() {
        try (MockedConstruction<WarehouseDAO> ignored1 = mockConstruction(WarehouseDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignored2 = mockConstruction(InventoryTransactionDAO.class);
             MockedConstruction<AssetDAO> ignored3 = mockConstruction(AssetDAO.class)) {

            WarehouseService service = new WarehouseService();
            assertThrows(IllegalArgumentException.class,
                    () -> service.addWarehouse(new Warehouse(0, "  ", "Atatürk Cad. No:1")));
        }
    }

    @Test
    @DisplayName("Lokasyon adresi null olduğunda IllegalArgumentException fırlatmalı")
    void addWarehouse_nullAddress_throwsException() {
        try (MockedConstruction<WarehouseDAO> ignored1 = mockConstruction(WarehouseDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignored2 = mockConstruction(InventoryTransactionDAO.class);
             MockedConstruction<AssetDAO> ignored3 = mockConstruction(AssetDAO.class)) {

            WarehouseService service = new WarehouseService();
            assertThrows(IllegalArgumentException.class,
                    () -> service.addWarehouse(new Warehouse(0, "Merkez Depo", null)));
        }
    }

    @Test
    @DisplayName("Lokasyon adresi boş string olduğunda IllegalArgumentException fırlatmalı")
    void addWarehouse_emptyAddress_throwsException() {
        try (MockedConstruction<WarehouseDAO> ignored1 = mockConstruction(WarehouseDAO.class);
             MockedConstruction<InventoryTransactionDAO> ignored2 = mockConstruction(InventoryTransactionDAO.class);
             MockedConstruction<AssetDAO> ignored3 = mockConstruction(AssetDAO.class)) {

            WarehouseService service = new WarehouseService();
            assertThrows(IllegalArgumentException.class,
                    () -> service.addWarehouse(new Warehouse(0, "Merkez Depo", "   ")));
        }
    }

    @Test
    @DisplayName("Geçerli depo eklendiğinde DAO insert çağrılmalı ve true dönmeli")
    void addWarehouse_validWarehouse_returnsTrueAndCallsDao() {
        try (MockedConstruction<WarehouseDAO> mockedDao = mockConstruction(WarehouseDAO.class,
                     (mock, ctx) -> when(mock.insert(any())).thenReturn(true));
             MockedConstruction<InventoryTransactionDAO> ignored2 = mockConstruction(InventoryTransactionDAO.class);
             MockedConstruction<AssetDAO> ignored3 = mockConstruction(AssetDAO.class)) {

            WarehouseService service = new WarehouseService();
            Warehouse warehouse = new Warehouse(0, "Merkez Depo", "Atatürk Cad. No:1, İstanbul");

            boolean result = service.addWarehouse(warehouse);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).insert(warehouse);
        }
    }

    @Test
    @DisplayName("updateWarehouse çağrıldığında DAO update çağrılmalı ve sonucu dönmeli")
    void updateWarehouse_callsDaoUpdate() {
        try (MockedConstruction<WarehouseDAO> mockedDao = mockConstruction(WarehouseDAO.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(true));
             MockedConstruction<InventoryTransactionDAO> ignored2 = mockConstruction(InventoryTransactionDAO.class);
             MockedConstruction<AssetDAO> ignored3 = mockConstruction(AssetDAO.class)) {

            WarehouseService service = new WarehouseService();
            Warehouse warehouse = new Warehouse(1, "Güncellenen Depo", "Yeni Adres No:5");

            boolean result = service.updateWarehouse(warehouse);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).update(warehouse);
        }
    }

    @Test
    @DisplayName("Stok hareketi ve demirbaşı olmayan depo başarıyla silinmeli")
    void deleteWarehouse_noConstraints_callsDaoDelete() {
        try (MockedConstruction<WarehouseDAO> mockedWh = mockConstruction(WarehouseDAO.class,
                     (mock, ctx) -> when(mock.delete(anyInt())).thenReturn(true));
             MockedConstruction<InventoryTransactionDAO> mockedTx = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.countByWarehouseId(anyInt())).thenReturn(0));
             MockedConstruction<AssetDAO> mockedAsset = mockConstruction(AssetDAO.class,
                     (mock, ctx) -> when(mock.countByWarehouseId(anyInt())).thenReturn(0))) {

            WarehouseService service = new WarehouseService();

            boolean result = service.deleteWarehouse(1);

            assertTrue(result);
            verify(mockedWh.constructed().get(0), times(1)).delete(1);
        }
    }

    @Test
    @DisplayName("Stok hareketi olan depo silinememeli → IllegalStateException")
    void deleteWarehouse_hasTransactions_throwsException() {
        try (MockedConstruction<WarehouseDAO> ignored1 = mockConstruction(WarehouseDAO.class);
             MockedConstruction<InventoryTransactionDAO> mockedTx = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.countByWarehouseId(1)).thenReturn(5));
             MockedConstruction<AssetDAO> ignored3 = mockConstruction(AssetDAO.class)) {

            WarehouseService service = new WarehouseService();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.deleteWarehouse(1));
            assertTrue(ex.getMessage().contains("5"));
        }
    }

    @Test
    @DisplayName("Demirbaşı olan depo silinememeli → IllegalStateException")
    void deleteWarehouse_hasAssets_throwsException() {
        try (MockedConstruction<WarehouseDAO> ignored1 = mockConstruction(WarehouseDAO.class);
             MockedConstruction<InventoryTransactionDAO> mockedTx = mockConstruction(InventoryTransactionDAO.class,
                     (mock, ctx) -> when(mock.countByWarehouseId(anyInt())).thenReturn(0));
             MockedConstruction<AssetDAO> mockedAsset = mockConstruction(AssetDAO.class,
                     (mock, ctx) -> when(mock.countByWarehouseId(2)).thenReturn(3))) {

            WarehouseService service = new WarehouseService();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.deleteWarehouse(2));
            assertTrue(ex.getMessage().contains("3"));
        }
    }

    @Test
    @DisplayName("getAllWarehouses çağrıldığında DAO getAll sonucu dönmeli")
    void getAllWarehouses_returnsList() {
        List<Warehouse> mockList = Arrays.asList(
                new Warehouse(1, "Merkez Depo", "İstanbul"),
                new Warehouse(2, "Kadıköy Şube", "Kadıköy, İstanbul")
        );

        try (MockedConstruction<WarehouseDAO> mockedDao = mockConstruction(WarehouseDAO.class,
                     (mock, ctx) -> when(mock.getAll()).thenReturn(mockList));
             MockedConstruction<InventoryTransactionDAO> ignored2 = mockConstruction(InventoryTransactionDAO.class);
             MockedConstruction<AssetDAO> ignored3 = mockConstruction(AssetDAO.class)) {

            WarehouseService service = new WarehouseService();

            List<Warehouse> result = service.getAllWarehouses();

            assertEquals(2, result.size());
            verify(mockedDao.constructed().get(0), times(1)).getAll();
        }
    }
}
