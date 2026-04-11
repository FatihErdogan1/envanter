package org.example.controller;

import org.example.model.entity.Asset;
import org.example.model.entity.AssetAssignment;
import org.example.model.entity.AssetMaintenance;
import org.example.model.entity.AssetTransfer;
import org.example.model.entity.Supplier;
import org.example.model.entity.User;
import org.example.model.entity.Warehouse;
import org.example.model.enums.AssetStatus;
import org.example.service.AssetService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AssetController {
    private final AssetService assetService;

    public AssetController() {
        this.assetService = new AssetService();
    }

    public List<Asset> getAllAssets() {
        return assetService.getAllAssets();
    }

    /** Her depo için durum bazlı demirbaş sayısını döner. */
    public List<Object[]> getWarehouseStatusSummary() {
        return assetService.getWarehouseStatusSummary();
    }

    public boolean addAsset(Supplier supplier, Warehouse warehouse, String serialNumber, String name, String dateText) throws IllegalArgumentException {
        try {
            LocalDate purchaseDate = LocalDate.parse(dateText); // Beklenen format: YYYY-MM-DD

            Asset asset = new Asset();
            asset.setSupplier(supplier);
            asset.setWarehouse(warehouse);
            asset.setSerialNumber(serialNumber);
            asset.setName(name);
            asset.setPurchaseDate(purchaseDate);
            asset.setStatus(AssetStatus.AVAILABLE);

            return assetService.addAsset(asset);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Geçersiz tarih formatı. Lütfen YYYY-AA-GG formatında giriniz.");
        }
    }

    public boolean retireAsset(int id) throws IllegalStateException {
        // 1. Önce veritabanından güncel asset nesnesini bulalım
        Asset asset = assetService.getAssetById(id);

        if (asset != null) {
            // 2. Servis katmanındaki o "unused" metodu burada kullanıyoruz
            return assetService.retireAsset(asset);
        }
        return false;
    }

    public boolean deleteAsset(int id) {
        return assetService.deleteAsset(id);
    }

    public boolean updateAsset(int id, Supplier supplier, Warehouse warehouse, String serialNumber, String name, String dateText, String statusStr) {
        try {
            Asset asset = new Asset();
            asset.setId(id);
            asset.setSupplier(supplier);
            asset.setWarehouse(warehouse);
            asset.setSerialNumber(serialNumber);
            asset.setName(name);
            asset.setPurchaseDate(java.time.LocalDate.parse(dateText));
            asset.setStatus(org.example.model.enums.AssetStatus.valueOf(statusStr));

            return assetService.updateAsset(asset);
        } catch (Exception e) {
            throw new IllegalArgumentException("Veri formatı hatalı: " + e.getMessage());
        }
    }

    /** Demirbaşı belirtilen kullanıcıya zimmete verir. */
    public void assignAsset(int assetId, User user, String notes) throws IllegalStateException {
        assetService.assignAsset(assetId, user, notes);
    }

    /** Aktif zimmeti kapatır, demirbaşı AVAILABLE'a döndürür. */
    public void returnAsset(int assetId) throws IllegalStateException {
        assetService.returnAsset(assetId);
    }

    /** Demirbaşın aktif zimmetini döner; yoksa null. */
    public AssetAssignment getActiveAssignment(int assetId) {
        return assetService.getActiveAssignment(assetId);
    }

    /** Demirbaşın tüm zimmet geçmişini döner. */
    public List<AssetAssignment> getAssignmentHistory(int assetId) {
        return assetService.getAssignmentHistory(assetId);
    }

    /** Demirbaşı başka bir depoya transfer eder. */
    public void transferAsset(int assetId, Warehouse fromWarehouse, Warehouse toWarehouse, String notes)
            throws IllegalStateException {
        assetService.transferAsset(assetId, fromWarehouse, toWarehouse, notes);
    }

    /** Demirbaşı bakıma alır. */
    public void startMaintenance(int assetId, String description, String notes)
            throws IllegalStateException {
        assetService.startMaintenance(assetId, description, notes);
    }

    /** Aktif bakımı tamamlar, demirbaşı AVAILABLE'a döndürür. */
    public void endMaintenance(int assetId) throws IllegalStateException {
        assetService.endMaintenance(assetId);
    }

    /** Demirbaşın tüm transfer geçmişini döner. */
    public List<AssetTransfer> getTransferHistory(int assetId) {
        return assetService.getTransferHistory(assetId);
    }

    /** Demirbaşın tüm bakım geçmişini döner. */
    public List<AssetMaintenance> getMaintenanceHistory(int assetId) {
        return assetService.getMaintenanceHistory(assetId);
    }
}
