package org.example.model.entity;

import java.time.LocalDateTime;

/** Bir demirbaşın depolar arası transfer kaydını temsil eder. */
public class AssetTransfer {

    private int id;
    private int assetId;
    private Warehouse fromWarehouse;
    private Warehouse toWarehouse;
    private LocalDateTime transferDate;
    private String notes;

    public AssetTransfer() {}

    public int getId()                          { return id; }
    public void setId(int id)                  { this.id = id; }

    public int getAssetId()                     { return assetId; }
    public void setAssetId(int assetId)        { this.assetId = assetId; }

    public Warehouse getFromWarehouse()                          { return fromWarehouse; }
    public void setFromWarehouse(Warehouse fromWarehouse)       { this.fromWarehouse = fromWarehouse; }

    public Warehouse getToWarehouse()                           { return toWarehouse; }
    public void setToWarehouse(Warehouse toWarehouse)          { this.toWarehouse = toWarehouse; }

    public LocalDateTime getTransferDate()                         { return transferDate; }
    public void setTransferDate(LocalDateTime transferDate)       { this.transferDate = transferDate; }

    public String getNotes()                    { return notes; }
    public void setNotes(String notes)         { this.notes = notes; }
}
