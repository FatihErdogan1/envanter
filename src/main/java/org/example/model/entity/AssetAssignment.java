package org.example.model.entity;

import java.time.LocalDateTime;

/**
 * Bir demirbaşın bir kullanıcıya zimmetlenme kaydını temsil eder.
 * returnDate null ise zimmet hâlâ aktiftir.
 */
public class AssetAssignment {

    private int id;
    private int assetId;
    private User user;                  // zimmetlenen kişi
    private LocalDateTime assignedDate; // zimmet başlangıç tarihi
    private LocalDateTime returnDate;   // null = hâlâ zimmetli
    private String notes;

    public AssetAssignment() {}

    public int getId()                       { return id; }
    public void setId(int id)               { this.id = id; }

    public int getAssetId()                  { return assetId; }
    public void setAssetId(int assetId)     { this.assetId = assetId; }

    public User getUser()                    { return user; }
    public void setUser(User user)          { this.user = user; }

    public LocalDateTime getAssignedDate()                         { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate)       { this.assignedDate = assignedDate; }

    public LocalDateTime getReturnDate()                           { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate)           { this.returnDate = returnDate; }

    public String getNotes()                 { return notes; }
    public void setNotes(String notes)      { this.notes = notes; }

    public boolean isActive() { return returnDate == null; }
}
