package org.example.model.entity;

import java.time.LocalDateTime;
import org.example.model.enums.TransactionType;

public class InventoryTransaction {
    private int id;
    private Product product; // İlişki
    private Warehouse warehouse; // Kaynak depo (IN/OUT için tek depo; TRANSFER için çıkış deposu)
    private Warehouse destinationWarehouse; // Yalnızca TRANSFER işlemlerinde dolu: hedef depo
    private User user; // İlişki (İşlemi kim yaptı?)
    private TransactionType type;
    private int quantity;
    private LocalDateTime transactionDate;
    private String notes;

    public InventoryTransaction() {}

    public InventoryTransaction(int id, Product product, Warehouse warehouse, Warehouse destinationWarehouse, User user, TransactionType type, int quantity, LocalDateTime transactionDate, String notes) {
        this.id = id;
        this.product = product;
        this.warehouse = warehouse;
        this.destinationWarehouse = destinationWarehouse;
        this.user = user;
        this.type = type;
        this.quantity = quantity;
        this.transactionDate = transactionDate;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Warehouse getDestinationWarehouse() {
        return destinationWarehouse;
    }

    public void setDestinationWarehouse(Warehouse destinationWarehouse) {
        this.destinationWarehouse = destinationWarehouse;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
