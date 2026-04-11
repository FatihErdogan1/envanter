package org.example.service;

import org.example.dao.impl.AssetAssignmentDAO;
import org.example.dao.impl.AssetDAO;
import org.example.dao.impl.AssetMaintenanceDAO;
import org.example.dao.impl.AssetTransferDAO;
import org.example.model.entity.Asset;
import org.example.model.entity.AssetAssignment;
import org.example.model.entity.AssetMaintenance;
import org.example.model.entity.AssetTransfer;
import org.example.model.entity.User;
import org.example.model.entity.Warehouse;
import org.example.model.enums.AssetStatus;

import java.time.LocalDate;
import java.util.List;

public class AssetService {

    private final AssetDAO assetDAO;
    private final AssetAssignmentDAO assignmentDAO;
    private final AssetTransferDAO transferDAO;
    private final AssetMaintenanceDAO maintenanceDAO;

    public AssetService() {
        this.assetDAO      = new AssetDAO();
        this.assignmentDAO = new AssetAssignmentDAO();
        this.transferDAO   = new AssetTransferDAO();
        this.maintenanceDAO = new AssetMaintenanceDAO();
    }

    public boolean addAsset(Asset asset) throws IllegalArgumentException {
        // İş Kuralı 1: Seri numarası boş olamaz
        if (asset.getSerialNumber() == null || asset.getSerialNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Demirbaş seri numarası zorunludur!");
        }

        // İş Kuralı 2: Satın alma tarihi bugünden ileri bir tarih olamaz
        if (asset.getPurchaseDate() != null && asset.getPurchaseDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Satın alma tarihi gelecekte bir tarih olamaz.");
        }

        // Yeni eklenen bir demirbaş varsayılan olarak AVAILABLE (Kullanıma Hazır) olmalıdır
        if (asset.getStatus() == null) {
            asset.setStatus(AssetStatus.AVAILABLE);
        }

        return assetDAO.insert(asset);
    }

    public boolean retireAsset(Asset asset) throws IllegalStateException {
        if (asset.getStatus() == AssetStatus.IN_USE) {
            throw new IllegalStateException("Bu demirbaş şu an bir personelde zimmetli. Önce zimmeti düşürülmelidir.");
        }
        return assetDAO.updateStatus(asset.getId(), AssetStatus.RETIRED);
    }

    /**
     * Demirbaşı belirtilen kullanıcıya zimmete verir.
     * Kural: yalnızca AVAILABLE demirbaşlar zimmetlenebilir.
     */
    public void assignAsset(int assetId, User user, String notes) throws IllegalStateException {
        if (user == null) {
            throw new IllegalArgumentException("Zimmet için kullanıcı seçilmelidir.");
        }
        AssetAssignment existing = assignmentDAO.getActiveByAssetId(assetId);
        if (existing != null) {
            User assignedTo = existing.getUser();
            String username = (assignedTo != null) ? assignedTo.getUsername() : "bilinmeyen kullanıcı";
            throw new IllegalStateException(
                "Bu demirbaş zaten " + username + " adlı personelde zimmetli.");
        }

        // Aktif zimmet yoksa durumu kontrol et (IN_USE ise tutarsız veri — yine de engelle)
        Asset asset = assetDAO.getById(assetId);
        if (asset == null) throw new IllegalStateException("Demirbaş bulunamadı.");
        if (asset.getStatus() == AssetStatus.MAINTENANCE) {
            throw new IllegalStateException("Bakımdaki demirbaş zimmetlenemez.");
        }
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new IllegalStateException("Hurda demirbaş zimmetlenemez.");
        }

        AssetAssignment assignment = new AssetAssignment();
        assignment.setAssetId(assetId);
        assignment.setUser(user);
        assignment.setNotes(notes);
        assignmentDAO.insert(assignment);
        assetDAO.updateStatus(assetId, AssetStatus.IN_USE);
    }

    /**
     * Aktif zimmeti kapatır ve demirbaşı AVAILABLE durumuna döndürür.
     */
    public void returnAsset(int assetId) throws IllegalStateException {
        AssetAssignment active = assignmentDAO.getActiveByAssetId(assetId);
        if (active == null) {
            throw new IllegalStateException("Bu demirbaşın aktif bir zimmeti bulunmuyor.");
        }
        assignmentDAO.closeAssignment(active.getId());
        assetDAO.updateStatus(assetId, AssetStatus.AVAILABLE);
    }

    public AssetAssignment getActiveAssignment(int assetId) {
        return assignmentDAO.getActiveByAssetId(assetId);
    }

    public List<AssetAssignment> getAssignmentHistory(int assetId) {
        return assignmentDAO.getHistoryByAssetId(assetId);
    }

    public boolean updateAsset(Asset asset) {
        return assetDAO.update(asset);
    }

    public boolean deleteAsset(int id) {
        return assetDAO.delete(id);
    }

    public List<Asset> getAllAssets() {
        return assetDAO.getAll();
    }

    public Asset getAssetById(int assetId) {
        return assetDAO.getById(assetId);
    }

    /** Her depo için durum bazlı demirbaş sayısını döner. */
    public List<Object[]> getWarehouseStatusSummary() {
        return assetDAO.getStatusSummaryByWarehouse();
    }

    /**
     * Demirbaşı başka bir depoya transfer eder.
     * Kural: yalnızca AVAILABLE demirbaşlar transfer edilebilir.
     * Kural: kaynak ve hedef depo farklı olmalıdır.
     */
    public void transferAsset(int assetId, Warehouse fromWarehouse, Warehouse toWarehouse, String notes)
            throws IllegalStateException {
        if (toWarehouse == null) throw new IllegalStateException("Hedef depo seçilmelidir.");
        if (fromWarehouse != null && fromWarehouse.getId() == toWarehouse.getId()) {
            throw new IllegalStateException("Kaynak ve hedef depo aynı olamaz.");
        }

        Asset asset = assetDAO.getById(assetId);
        if (asset == null) throw new IllegalStateException("Demirbaş bulunamadı.");
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new IllegalStateException("Hurda demirbaş transfer edilemez.");
        }
        if (asset.getStatus() == AssetStatus.IN_USE) {
            throw new IllegalStateException("Zimmetli demirbaş transfer edilemez. Önce zimmeti düşürün.");
        }
        if (asset.getStatus() == AssetStatus.MAINTENANCE) {
            throw new IllegalStateException("Bakımdaki demirbaş transfer edilemez. Önce bakımı tamamlayın.");
        }

        AssetTransfer transfer = new AssetTransfer();
        transfer.setAssetId(assetId);
        transfer.setFromWarehouse(fromWarehouse);
        transfer.setToWarehouse(toWarehouse);
        transfer.setNotes(notes);

        transferDAO.insert(transfer);
        assetDAO.updateWarehouseId(assetId, toWarehouse.getId());
    }

    /**
     * Demirbaşı bakıma alır; durumunu MAINTENANCE olarak günceller.
     * Kural: yalnızca AVAILABLE demirbaşlar bakıma alınabilir.
     */
    public void startMaintenance(int assetId, String description, String notes)
            throws IllegalStateException {
        Asset asset = assetDAO.getById(assetId);
        if (asset == null) throw new IllegalStateException("Demirbaş bulunamadı.");
        if (asset.getStatus() == AssetStatus.IN_USE) {
            throw new IllegalStateException("Zimmetli demirbaş bakıma alınamaz. Önce zimmeti düşürün.");
        }
        if (asset.getStatus() == AssetStatus.MAINTENANCE) {
            throw new IllegalStateException("Bu demirbaş zaten bakımda.");
        }
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new IllegalStateException("Hurda demirbaş bakıma alınamaz.");
        }

        AssetMaintenance maintenance = new AssetMaintenance();
        maintenance.setAssetId(assetId);
        maintenance.setDescription(description);
        maintenance.setNotes(notes);

        maintenanceDAO.insert(maintenance);
        assetDAO.updateStatus(assetId, AssetStatus.MAINTENANCE);
    }

    /**
     * Aktif bakım kaydını kapatır; demirbaşı AVAILABLE durumuna döndürür.
     */
    public void endMaintenance(int assetId) throws IllegalStateException {
        AssetMaintenance active = maintenanceDAO.getActiveByAssetId(assetId);
        if (active == null) {
            throw new IllegalStateException("Bu demirbaşın aktif bir bakım kaydı bulunmuyor.");
        }
        maintenanceDAO.closeActive(assetId);
        assetDAO.updateStatus(assetId, AssetStatus.AVAILABLE);
    }

    public List<AssetTransfer> getTransferHistory(int assetId) {
        return transferDAO.getHistoryByAssetId(assetId);
    }

    public List<AssetMaintenance> getMaintenanceHistory(int assetId) {
        return maintenanceDAO.getHistoryByAssetId(assetId);
    }
}
