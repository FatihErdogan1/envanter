package org.example.service;

import org.example.dao.impl.AssetDAO;
import org.example.dao.impl.InventoryTransactionDAO;
import org.example.dao.impl.WarehouseDAO;
import org.example.model.entity.Warehouse;

import java.util.List;

public class WarehouseService {

    private final WarehouseDAO warehouseDAO;
    private final InventoryTransactionDAO transactionDAO;
    private final AssetDAO assetDAO;

    public WarehouseService() {
        this.warehouseDAO   = new WarehouseDAO();
        this.transactionDAO = new InventoryTransactionDAO();
        this.assetDAO       = new AssetDAO();
    }

    /**
     * Yeni bir depo eklerken uygulanacak iş kuralları.
     */
    public boolean addWarehouse(Warehouse warehouse) throws IllegalArgumentException {
        // İş Kuralı 1: Depo adı zorunludur (Örn: Merkez Depo, Kadıköy Şube)
        if (warehouse.getName() == null || warehouse.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Depo adı boş bırakılamaz.");
        }

        // İş Kuralı 2: Depo lokasyon/adres bilgisi zorunludur
        if (warehouse.getLocationAddress() == null || warehouse.getLocationAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Depo lokasyon adresi boş bırakılamaz.");
        }

        return warehouseDAO.insert(warehouse);
    }

    public boolean updateWarehouse(Warehouse warehouse) {
        return warehouseDAO.update(warehouse);
    }

    public boolean deleteWarehouse(int id) throws IllegalStateException {
        int txCount = transactionDAO.countByWarehouseId(id);
        if (txCount > 0) {
            throw new IllegalStateException(
                "Bu depoya ait " + txCount + " adet stok hareketi kaydı bulunduğundan silinemez."
            );
        }

        int assetCount = assetDAO.countByWarehouseId(id);
        if (assetCount > 0) {
            throw new IllegalStateException(
                "Bu depoda " + assetCount + " adet demirbaş kaydı bulunduğundan silinemez."
            );
        }

        return warehouseDAO.delete(id);
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseDAO.getAll();
    }
}