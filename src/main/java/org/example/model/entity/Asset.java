package org.example.model.entity;

import java.time.LocalDate;
import org.example.model.enums.AssetStatus;

public class Asset {
    private int id;
    private Supplier supplier; // İlişki: Supplier nesnesi
    private Warehouse warehouse; // İlişki: Warehouse nesnesi
    private String serialNumber;
    private String name;
    private LocalDate purchaseDate;
    private AssetStatus status;
    /** Görüntüleme alanı — DB'ye yazılmaz, getAll() JOIN'den gelir. null = zimmetli değil. */
    private String assignedToUsername;

    public Asset() {}

    public Asset(int id, Supplier supplier, Warehouse warehouse, String serialNumber, String name, LocalDate purchaseDate, AssetStatus status) {
        this.id = id;
        this.supplier = supplier;
        this.warehouse = warehouse;
        this.serialNumber = serialNumber;
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public String getAssignedToUsername() {
        return assignedToUsername;
    }

    public void setAssignedToUsername(String assignedToUsername) {
        this.assignedToUsername = assignedToUsername;
    }

    @Override
    public String toString() {
        // JComboBox'ta sadece kategori adının görünmesini sağlar
        return this.name;
    }
}
