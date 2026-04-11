package org.example.controller;

import org.example.dao.impl.InventoryTransactionDAO;
import org.example.model.entity.InventoryTransaction;
import org.example.model.entity.Product;
import org.example.model.entity.User;
import org.example.model.entity.Warehouse;
import org.example.model.enums.TransactionType;
import org.example.service.InventoryService;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionController {
    private final InventoryService inventoryService;

    public TransactionController() {
        this.inventoryService = new InventoryService();
    }

    /**
     * IN / OUT işlemleri için (hedef depo yok).
     */
    public boolean processTransaction(Product product, Warehouse warehouse, User user,
                                      String typeStr, String quantityText, String notes)
            throws IllegalArgumentException {
        return processTransaction(product, warehouse, null, user, typeStr, quantityText, notes);
    }

    /**
     * Tüm işlem tipleri için ana metot.
     * TRANSFER işlemlerinde destinationWarehouse null olmamalıdır.
     */
    public boolean processTransaction(Product product, Warehouse sourceWarehouse,
                                      Warehouse destinationWarehouse, User user,
                                      String typeStr, String quantityText, String notes)
            throws IllegalArgumentException {
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Miktar alanına sadece tam sayı girilmelidir.");
        }

        TransactionType type;
        try {
            type = TransactionType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Hatalı işlem tipi seçildi.");
        }

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setWarehouse(sourceWarehouse);
        transaction.setDestinationWarehouse(destinationWarehouse);
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setNotes(notes);

        return inventoryService.processTransaction(transaction); // iş kuralı hataları olduğu gibi iletilir
    }
    public List<InventoryTransaction> getAllTransactions() {
        return new InventoryTransactionDAO().getAll();
    }

    /** Her (depo, ürün) çifti için hesaplanmış anlık stok listesini döner. */
    public List<Object[]> getWarehouseStockReport() {
        return inventoryService.getWarehouseStockReport();
    }
}
