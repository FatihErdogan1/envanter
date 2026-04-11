package org.example.model.entity;

import java.time.LocalDateTime;

/** Bir demirbaşın bakım/onarım kaydını temsil eder. endDate null ise bakım hâlâ sürmektedir. */
public class AssetMaintenance {

    private int id;
    private int assetId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;   // null = aktif bakım
    private String description;
    private String notes;

    public AssetMaintenance() {}

    public int getId()                          { return id; }
    public void setId(int id)                  { this.id = id; }

    public int getAssetId()                     { return assetId; }
    public void setAssetId(int assetId)        { this.assetId = assetId; }

    public LocalDateTime getStartDate()                          { return startDate; }
    public void setStartDate(LocalDateTime startDate)           { this.startDate = startDate; }

    public LocalDateTime getEndDate()                            { return endDate; }
    public void setEndDate(LocalDateTime endDate)               { this.endDate = endDate; }

    public String getDescription()              { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNotes()                    { return notes; }
    public void setNotes(String notes)         { this.notes = notes; }

    public boolean isActive() { return endDate == null; }
}
