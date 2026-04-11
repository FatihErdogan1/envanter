package org.example.service;

import org.example.dao.impl.InventoryTransactionDAO;
import org.example.dao.impl.ProductDAO;
import org.example.model.entity.InventoryTransaction;
import org.example.model.entity.Product;
import org.example.model.enums.TransactionType;

import java.util.List;

public class InventoryService {

    // Servis katmanı, DAO sınıfları ile iletişim kurar
    private final ProductDAO productDAO;
    private final InventoryTransactionDAO transactionDAO;

    // Bağımlılıkların enjekte edilmesi (Dependency Injection mantığı)
    public InventoryService() {
        this.productDAO = new ProductDAO();
        this.transactionDAO = new InventoryTransactionDAO();
    }

    /**
     * Stok hareketlerini (Giriş/Çıkış) yöneten ana iş mantığı.
     */
    public boolean processTransaction(InventoryTransaction transaction) throws IllegalArgumentException {
        Product product = transaction.getProduct();
        int currentStock = product.getQuantityInStock();
        int transactionQuantity = transaction.getQuantity();

        // İş Kuralı 3: İşlem miktarı 0 veya negatif olamaz
        if (transactionQuantity <= 0) {
            throw new IllegalArgumentException("İşlem miktarı 0'dan büyük olmalıdır.");
        }

        if (transaction.getType() == TransactionType.IN) {
            // Stok girişi: Mevcut stoğu artır
            product.setQuantityInStock(currentStock + transactionQuantity);

        } else if (transaction.getType() == TransactionType.OUT) {
            // Stok çıkışı için kritik kontrol
            // İş Kuralı 4: Stokta yeterli ürün yoksa çıkış yapılamaz!
            if (currentStock < transactionQuantity) {
                throw new IllegalArgumentException("Hata: Depoda yeterli stok yok! Mevcut stok: " + currentStock);
            }
            // Yeterli stok varsa düş
            product.setQuantityInStock(currentStock - transactionQuantity);

        } else if (transaction.getType() == TransactionType.TRANSFER) {
            // Kaynak depo kontrolü
            if (transaction.getWarehouse() == null) {
                throw new IllegalArgumentException("Transfer işlemi için kaynak depo belirtilmelidir.");
            }
            // Hedef depo kontrolü
            if (transaction.getDestinationWarehouse() == null) {
                throw new IllegalArgumentException("Transfer işlemi için hedef depo belirtilmelidir.");
            }
            // Aynı depo kontrolü
            if (transaction.getWarehouse().getId() == transaction.getDestinationWarehouse().getId()) {
                throw new IllegalArgumentException("Kaynak ve hedef depo aynı olamaz.");
            }
            // Yeterli stok kontrolü (fiziksel taşıma — genel toplam değişmez)
            if (currentStock < transactionQuantity) {
                throw new IllegalArgumentException(
                        "Hata: Transfer için yeterli stok yok! Mevcut stok: " + currentStock);
            }
            // Genel stok toplamı değişmediğinden product.quantityInStock güncellenmez;
            // hareket kaydı kaynak ve hedef depo bilgisiyle birlikte saklanır.
        }

        // 1. Stok hareketini kaydet
        boolean isTransactionSaved = transactionDAO.insert(transaction);

        // 2. Hareket kaydedildiyse stok miktarını güncelle.
        //    TRANSFER: global stok değişmediğinden productDAO.update gerekmez.
        if (isTransactionSaved) {
            if (transaction.getType() == TransactionType.TRANSFER) {
                return true;
            }
            return productDAO.update(product);
        }

        return false;
    }

    /** Her (depo, ürün) çifti için hesaplanmış anlık stok listesini döner. */
    public List<Object[]> getWarehouseStockReport() {
        return transactionDAO.getWarehouseStockReport();
    }
}
