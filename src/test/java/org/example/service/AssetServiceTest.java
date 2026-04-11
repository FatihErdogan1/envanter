package org.example.service;

import org.example.dao.impl.AssetAssignmentDAO;
import org.example.dao.impl.AssetDAO;
import org.example.dao.impl.AssetMaintenanceDAO;
import org.example.dao.impl.AssetTransferDAO;
import org.example.model.entity.Asset;
import org.example.model.entity.AssetAssignment;
import org.example.model.entity.AssetMaintenance;
import org.example.model.entity.User;
import org.example.model.entity.Warehouse;
import org.example.model.enums.AssetStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("AssetService İş Kuralları Testleri")
class AssetServiceTest {

    // ── Yardımcılar ──────────────────────────────────────────────────────────

    /** 4 DAO'yu birlikte mock'layan yardımcı — her testte tekrar yazmamak için. */
    private interface ThrowingConsumer { void accept() throws Exception; }

    /** AssetDAO'yu verilen mock kurucusu ile, diğer 3 DAO'yu varsayılan boş mock olarak açar. */
    private void withAllMocks(
            MockedConstruction.MockInitializer<AssetDAO> assetInit,
            ThrowingConsumer test) throws Exception {

        try (MockedConstruction<AssetDAO>            ma  = mockConstruction(AssetDAO.class,           assetInit);
             MockedConstruction<AssetAssignmentDAO>  mb  = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc  = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md  = mockConstruction(AssetMaintenanceDAO.class)) {
            test.accept();
        }
    }

    private Asset buildAvailableAsset() {
        Asset a = new Asset();
        a.setId(1);
        a.setSerialNumber("SN-001");
        a.setName("Laptop");
        a.setPurchaseDate(LocalDate.now());
        a.setStatus(AssetStatus.AVAILABLE);
        return a;
    }

    private Warehouse buildWarehouse(int id, String name) {
        Warehouse w = new Warehouse();
        w.setId(id);
        w.setName(name);
        return w;
    }

    // ── addAsset ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Seri numarası null olduğunda IllegalArgumentException fırlatmalı")
    void addAsset_serialNumberNull_throwsException() throws Exception {
        withAllMocks((mock, ctx) -> {}, () -> {
            AssetService service = new AssetService();
            Asset asset = new Asset();
            asset.setSerialNumber(null);
            assertThrows(IllegalArgumentException.class, () -> service.addAsset(asset));
        });
    }

    @Test
    @DisplayName("Seri numarası boş string olduğunda IllegalArgumentException fırlatmalı")
    void addAsset_serialNumberEmpty_throwsException() throws Exception {
        withAllMocks((mock, ctx) -> {}, () -> {
            AssetService service = new AssetService();
            Asset asset = new Asset();
            asset.setSerialNumber("   ");
            assertThrows(IllegalArgumentException.class, () -> service.addAsset(asset));
        });
    }

    @Test
    @DisplayName("Satın alma tarihi gelecekte ise IllegalArgumentException fırlatmalı")
    void addAsset_futurePurchaseDate_throwsException() throws Exception {
        withAllMocks((mock, ctx) -> {}, () -> {
            AssetService service = new AssetService();
            Asset asset = new Asset();
            asset.setSerialNumber("SN-001");
            asset.setPurchaseDate(LocalDate.now().plusDays(1));
            assertThrows(IllegalArgumentException.class, () -> service.addAsset(asset));
        });
    }

    @Test
    @DisplayName("Status null ise AVAILABLE olarak otomatik atanmalı")
    void addAsset_nullStatus_setsAvailable() throws Exception {
        withAllMocks((mock, ctx) -> when(mock.insert(any())).thenReturn(true), () -> {
            AssetService service = new AssetService();
            Asset asset = new Asset();
            asset.setSerialNumber("SN-001");
            asset.setPurchaseDate(LocalDate.now());
            asset.setStatus(null);

            service.addAsset(asset);

            assertEquals(AssetStatus.AVAILABLE, asset.getStatus());
        });
    }

    @Test
    @DisplayName("Geçerli demirbaş eklendiğinde DAO insert çağrılmalı ve true dönmeli")
    void addAsset_validAsset_returnsTrueAndCallsDao() throws Exception {
        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                    (mock, ctx) -> when(mock.insert(any())).thenReturn(true));
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();
            Asset asset = buildAvailableAsset();

            boolean result = service.addAsset(asset);

            assertTrue(result);
            verify(ma.constructed().get(0), times(1)).insert(asset);
        }
    }

    // ── retireAsset ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("IN_USE durumundaki demirbaş hurdaya ayrılamaz → IllegalStateException")
    void retireAsset_inUseStatus_throwsException() throws Exception {
        withAllMocks((mock, ctx) -> {}, () -> {
            AssetService service = new AssetService();
            Asset asset = new Asset();
            asset.setStatus(AssetStatus.IN_USE);
            assertThrows(IllegalStateException.class, () -> service.retireAsset(asset));
        });
    }

    @Test
    @DisplayName("AVAILABLE demirbaş hurdaya ayrıldığında updateStatus(RETIRED) çağrılmalı")
    void retireAsset_availableAsset_callsUpdateStatus() throws Exception {
        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                    (mock, ctx) -> when(mock.updateStatus(anyInt(), eq(AssetStatus.RETIRED))).thenReturn(true));
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();
            Asset asset = new Asset();
            asset.setId(1);
            asset.setStatus(AssetStatus.AVAILABLE);

            boolean result = service.retireAsset(asset);

            assertTrue(result);
            verify(ma.constructed().get(0), times(1)).updateStatus(1, AssetStatus.RETIRED);
        }
    }

    @Test
    @DisplayName("MAINTENANCE durumundaki demirbaş hurdaya ayrılabilir")
    void retireAsset_maintenanceAsset_succeeds() throws Exception {
        withAllMocks(
            (mock, ctx) -> when(mock.updateStatus(anyInt(), eq(AssetStatus.RETIRED))).thenReturn(true),
            () -> {
                AssetService service = new AssetService();
                Asset asset = new Asset();
                asset.setId(1);
                asset.setStatus(AssetStatus.MAINTENANCE);
                assertDoesNotThrow(() -> service.retireAsset(asset));
            }
        );
    }

    // ── assignAsset ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("user null ise IllegalArgumentException fırlatmalı")
    void assignAsset_nullUser_throwsException() throws Exception {
        withAllMocks((mock, ctx) -> {}, () -> {
            AssetService service = new AssetService();
            assertThrows(IllegalArgumentException.class,
                    () -> service.assignAsset(1, null, "not"));
        });
    }

    @Test
    @DisplayName("Demirbaş zaten zimmetliyse IllegalStateException fırlatmalı")
    void assignAsset_alreadyAssigned_throwsException() throws Exception {
        AssetAssignment existing = new AssetAssignment();
        User assignedUser = new User();
        assignedUser.setUsername("ahmet");
        existing.setUser(assignedUser);

        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class);
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class,
                     (mock, ctx) -> when(mock.getActiveByAssetId(1)).thenReturn(existing));
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();
            User user = new User();
            user.setUsername("mehmet");

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.assignAsset(1, user, ""));
            assertTrue(ex.getMessage().contains("ahmet"));
        }
    }

    @Test
    @DisplayName("Demirbaş MAINTENANCE durumundaysa zimmetlenemez → IllegalStateException")
    void assignAsset_maintenanceAsset_throwsException() throws Exception {
        Asset asset = new Asset();
        asset.setId(1);
        asset.setStatus(AssetStatus.MAINTENANCE);

        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                     (mock, ctx) -> when(mock.getById(1)).thenReturn(asset));
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class,
                     (mock, ctx) -> when(mock.getActiveByAssetId(1)).thenReturn(null));
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();
            User user = new User();
            user.setUsername("mehmet");

            assertThrows(IllegalStateException.class,
                    () -> service.assignAsset(1, user, ""));
        }
    }

    @Test
    @DisplayName("AVAILABLE demirbaş geçerli kullanıcıya zimmetlenebilir; durum IN_USE olmalı")
    void assignAsset_availableAsset_validUser_succeeds() throws Exception {
        Asset asset = buildAvailableAsset();

        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                     (mock, ctx) -> {
                         when(mock.getById(1)).thenReturn(asset);
                         when(mock.updateStatus(anyInt(), eq(AssetStatus.IN_USE))).thenReturn(true);
                     });
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class,
                     (mock, ctx) -> {
                         when(mock.getActiveByAssetId(1)).thenReturn(null);
                         when(mock.insert(any())).thenReturn(true);
                     });
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();
            User user = new User();
            user.setUsername("mehmet");

            assertDoesNotThrow(() -> service.assignAsset(1, user, "Zimmet notu"));

            verify(mb.constructed().get(0), times(1)).insert(any());
            verify(ma.constructed().get(0), times(1)).updateStatus(1, AssetStatus.IN_USE);
        }
    }

    // ── updateAsset / deleteAsset / getAllAssets / getAssetById ──────────────

    @Test
    @DisplayName("updateAsset çağrıldığında DAO update çağrılmalı ve sonucu dönmeli")
    void updateAsset_callsDaoUpdate() throws Exception {
        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                    (mock, ctx) -> when(mock.update(any())).thenReturn(true));
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();
            Asset asset = new Asset();
            asset.setSerialNumber("SN-001");

            boolean result = service.updateAsset(asset);

            assertTrue(result);
            verify(ma.constructed().get(0), times(1)).update(asset);
        }
    }

    @Test
    @DisplayName("deleteAsset çağrıldığında DAO delete çağrılmalı ve sonucu dönmeli")
    void deleteAsset_callsDaoDelete() throws Exception {
        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                    (mock, ctx) -> when(mock.delete(anyInt())).thenReturn(true));
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();

            boolean result = service.deleteAsset(1);

            assertTrue(result);
            verify(ma.constructed().get(0), times(1)).delete(1);
        }
    }

    @Test
    @DisplayName("getAllAssets çağrıldığında DAO getAll sonucu dönmeli")
    void getAllAssets_returnsList() throws Exception {
        List<Asset> mockList = Arrays.asList(new Asset(), new Asset());
        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                    (mock, ctx) -> when(mock.getAll()).thenReturn(mockList));
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();

            List<Asset> result = service.getAllAssets();

            assertEquals(2, result.size());
            verify(ma.constructed().get(0), times(1)).getAll();
        }
    }

    @Test
    @DisplayName("getAssetById çağrıldığında DAO getById sonucu dönmeli")
    void getAssetById_returnsAsset() throws Exception {
        Asset mockAsset = new Asset();
        mockAsset.setSerialNumber("SN-001");
        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                    (mock, ctx) -> when(mock.getById(1)).thenReturn(mockAsset));
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();

            Asset result = service.getAssetById(1);

            assertNotNull(result);
            assertEquals("SN-001", result.getSerialNumber());
            verify(ma.constructed().get(0), times(1)).getById(1);
        }
    }

    // ── transferAsset ────────────────────────────────────────────────────────

    @Test
    @DisplayName("AVAILABLE demirbaş başka depoya transfer edilebilir")
    void transferAsset_available_updatesWarehouseAndInsertsRecord() throws Exception {
        Asset asset = buildAvailableAsset();
        Warehouse from = buildWarehouse(1, "Depo A");
        Warehouse to   = buildWarehouse(2, "Depo B");

        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                    (mock, ctx) -> {
                        when(mock.getById(1)).thenReturn(asset);
                        when(mock.updateWarehouseId(anyInt(), anyInt())).thenReturn(true);
                    });
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class,
                    (mock, ctx) -> when(mock.insert(any())).thenReturn(true));
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class)) {

            AssetService service = new AssetService();
            assertDoesNotThrow(() -> service.transferAsset(1, from, to, "Test transfer"));

            verify(mc.constructed().get(0), times(1)).insert(any());
            verify(ma.constructed().get(0), times(1)).updateWarehouseId(1, 2);
        }
    }

    @Test
    @DisplayName("Kaynak ve hedef depo aynıysa IllegalStateException fırlatmalı")
    void transferAsset_sameWarehouse_throwsException() throws Exception {
        Asset asset = buildAvailableAsset();
        Warehouse same = buildWarehouse(1, "Depo A");

        withAllMocks((mock, ctx) -> when(mock.getById(1)).thenReturn(asset), () -> {
            AssetService service = new AssetService();
            assertThrows(IllegalStateException.class,
                    () -> service.transferAsset(1, same, same, ""));
        });
    }

    @Test
    @DisplayName("RETIRED demirbaş transfer edilemez → IllegalStateException")
    void transferAsset_retiredAsset_throwsException() throws Exception {
        Asset asset = new Asset(); asset.setId(1); asset.setStatus(AssetStatus.RETIRED);
        Warehouse from = buildWarehouse(1, "A"); Warehouse to = buildWarehouse(2, "B");

        withAllMocks((mock, ctx) -> when(mock.getById(1)).thenReturn(asset), () -> {
            AssetService service = new AssetService();
            assertThrows(IllegalStateException.class,
                    () -> service.transferAsset(1, from, to, ""));
        });
    }

    @Test
    @DisplayName("IN_USE demirbaş transfer edilemez → IllegalStateException")
    void transferAsset_inUseAsset_throwsException() throws Exception {
        Asset asset = new Asset(); asset.setId(1); asset.setStatus(AssetStatus.IN_USE);
        Warehouse from = buildWarehouse(1, "A"); Warehouse to = buildWarehouse(2, "B");

        withAllMocks((mock, ctx) -> when(mock.getById(1)).thenReturn(asset), () -> {
            AssetService service = new AssetService();
            assertThrows(IllegalStateException.class,
                    () -> service.transferAsset(1, from, to, ""));
        });
    }

    // ── startMaintenance ─────────────────────────────────────────────────────

    @Test
    @DisplayName("AVAILABLE demirbaş bakıma alınabilir; durum MAINTENANCE olmalı")
    void startMaintenance_availableAsset_setsMaintenanceStatus() throws Exception {
        Asset asset = buildAvailableAsset();

        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                    (mock, ctx) -> {
                        when(mock.getById(1)).thenReturn(asset);
                        when(mock.updateStatus(anyInt(), eq(AssetStatus.MAINTENANCE))).thenReturn(true);
                    });
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class,
                    (mock, ctx) -> when(mock.insert(any())).thenReturn(true))) {

            AssetService service = new AssetService();
            assertDoesNotThrow(() -> service.startMaintenance(1, "Fan arızası", ""));

            verify(md.constructed().get(0), times(1)).insert(any());
            verify(ma.constructed().get(0), times(1)).updateStatus(1, AssetStatus.MAINTENANCE);
        }
    }

    @Test
    @DisplayName("IN_USE demirbaş bakıma alınamaz → IllegalStateException")
    void startMaintenance_inUseAsset_throwsException() throws Exception {
        Asset asset = new Asset(); asset.setId(1); asset.setStatus(AssetStatus.IN_USE);

        withAllMocks((mock, ctx) -> when(mock.getById(1)).thenReturn(asset), () -> {
            AssetService service = new AssetService();
            assertThrows(IllegalStateException.class,
                    () -> service.startMaintenance(1, "desc", ""));
        });
    }

    @Test
    @DisplayName("Zaten MAINTENANCE olan demirbaş tekrar bakıma alınamaz → IllegalStateException")
    void startMaintenance_alreadyInMaintenance_throwsException() throws Exception {
        Asset asset = new Asset(); asset.setId(1); asset.setStatus(AssetStatus.MAINTENANCE);

        withAllMocks((mock, ctx) -> when(mock.getById(1)).thenReturn(asset), () -> {
            AssetService service = new AssetService();
            assertThrows(IllegalStateException.class,
                    () -> service.startMaintenance(1, "desc", ""));
        });
    }

    // ── endMaintenance ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Aktif bakım varken endMaintenance; kaydı kapatır ve durum AVAILABLE olur")
    void endMaintenance_activeMaintenance_closesRecordAndSetsAvailable() throws Exception {
        AssetMaintenance active = new AssetMaintenance();
        active.setId(10);

        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class,
                    (mock, ctx) -> when(mock.updateStatus(anyInt(), eq(AssetStatus.AVAILABLE))).thenReturn(true));
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class,
                    (mock, ctx) -> {
                        when(mock.getActiveByAssetId(1)).thenReturn(active);
                        when(mock.closeActive(1)).thenReturn(true);
                    })) {

            AssetService service = new AssetService();
            assertDoesNotThrow(() -> service.endMaintenance(1));

            verify(md.constructed().get(0), times(1)).closeActive(1);
            verify(ma.constructed().get(0), times(1)).updateStatus(1, AssetStatus.AVAILABLE);
        }
    }

    @Test
    @DisplayName("Aktif bakım kaydı yokken endMaintenance → IllegalStateException")
    void endMaintenance_noActiveMaintenance_throwsException() throws Exception {
        try (MockedConstruction<AssetDAO>            ma = mockConstruction(AssetDAO.class);
             MockedConstruction<AssetAssignmentDAO>  mb = mockConstruction(AssetAssignmentDAO.class);
             MockedConstruction<AssetTransferDAO>    mc = mockConstruction(AssetTransferDAO.class);
             MockedConstruction<AssetMaintenanceDAO> md = mockConstruction(AssetMaintenanceDAO.class,
                    (mock, ctx) -> when(mock.getActiveByAssetId(anyInt())).thenReturn(null))) {

            AssetService service = new AssetService();
            assertThrows(IllegalStateException.class, () -> service.endMaintenance(1));
        }
    }
}
