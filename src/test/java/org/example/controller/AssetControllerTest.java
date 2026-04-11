package org.example.controller;

import org.example.model.entity.Asset;
import org.example.model.entity.Supplier;
import org.example.model.entity.Warehouse;
import org.example.service.AssetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Service testlerinde kapsanmayan Controller-katmanı mantığını test eder:
 * 1. UI'dan gelen tarih String'inin LocalDate parse işlemi ve
 *    DateTimeParseException → IllegalArgumentException dönüşümü.
 * 2. retireAsset: getById null döndürdüğünde false dönmesi
 *    (Service.retireAsset doğrudan Asset alır; null kontrolü Controller'a ait).
 * 3. updateAsset: geçersiz status String'inin IllegalArgumentException üretmesi.
 */
@DisplayName("AssetController Katman Testleri")
class AssetControllerTest {

    private final Supplier dummySupplier  = new Supplier(1, "Tedarikçi A", null, null, null);
    private final Warehouse dummyWarehouse = new Warehouse(1, "Merkez Depo", "Adres");

    // ── addAsset ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addAsset: geçersiz tarih formatı girildiğinde IllegalArgumentException fırlatmalı")
    void addAsset_invalidDateFormat_throwsException() {
        try (MockedConstruction<AssetService> ignored = mockConstruction(AssetService.class)) {
            AssetController controller = new AssetController();

            assertThrows(IllegalArgumentException.class,
                    () -> controller.addAsset(dummySupplier, dummyWarehouse, "SN-001", "Laptop", "31-12-2024"));
        }
    }

    @Test
    @DisplayName("addAsset: tarih tamamen metinsel ise IllegalArgumentException fırlatmalı")
    void addAsset_textAsDate_throwsException() {
        try (MockedConstruction<AssetService> ignored = mockConstruction(AssetService.class)) {
            AssetController controller = new AssetController();

            assertThrows(IllegalArgumentException.class,
                    () -> controller.addAsset(dummySupplier, dummyWarehouse, "SN-001", "Laptop", "bugun"));
        }
    }

    // ── retireAsset ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("retireAsset: demirbaş ID ile bulunamazsa (getById→null) false dönmeli")
    void retireAsset_assetNotFound_returnsFalse() {
        try (MockedConstruction<AssetService> mockedService = mockConstruction(AssetService.class,
                (mock, ctx) -> when(mock.getAssetById(99)).thenReturn(null))) {

            AssetController controller = new AssetController();

            boolean result = controller.retireAsset(99);

            assertFalse(result);
            // retireAsset(asset) hiç çağrılmamalı
            verify(mockedService.constructed().get(0), never()).retireAsset(any(Asset.class));
        }
    }

    // ── updateAsset ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateAsset: geçersiz tarih formatı girildiğinde IllegalArgumentException fırlatmalı")
    void updateAsset_invalidDateFormat_throwsException() {
        try (MockedConstruction<AssetService> ignored = mockConstruction(AssetService.class)) {
            AssetController controller = new AssetController();

            assertThrows(IllegalArgumentException.class,
                    () -> controller.updateAsset(1, dummySupplier, dummyWarehouse,
                            "SN-001", "Laptop", "31/12/2024", "AVAILABLE"));
        }
    }

    @Test
    @DisplayName("updateAsset: geçersiz status metni girildiğinde IllegalArgumentException fırlatmalı")
    void updateAsset_invalidStatusString_throwsException() {
        try (MockedConstruction<AssetService> ignored = mockConstruction(AssetService.class)) {
            AssetController controller = new AssetController();

            assertThrows(IllegalArgumentException.class,
                    () -> controller.updateAsset(1, dummySupplier, dummyWarehouse,
                            "SN-001", "Laptop", "2024-01-15", "GECERSIZ_DURUM"));
        }
    }
}
