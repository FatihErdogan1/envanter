package org.example.service;

import org.example.dao.impl.InventoryTransactionDAO;
import org.example.dao.impl.ProductDAO;
import org.example.model.entity.Product;

import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;
    private final InventoryTransactionDAO transactionDAO;

    public ProductService() {
        this.productDAO     = new ProductDAO();
        this.transactionDAO = new InventoryTransactionDAO();
    }

    /**
     * Yeni bir ürün sisteme (kataloğa) eklenirken işletilecek iş kuralları.
     */
    public boolean addProduct(Product product) throws IllegalArgumentException {
        // İş Kuralı 1: Stok Kodu (SKU) ve İsim boş olamaz
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Ürün stok kodu (SKU) boş bırakılamaz.");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ürün adı boş bırakılamaz.");
        }

        // İş Kuralı 2: Ürünün mutlaka bir kategorisi olmalıdır
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Ürün için geçerli bir kategori seçilmelidir.");
        }

        // İş Kuralı 3: Mantıksal kontroller (Negatif fiyat veya stok olamaz)
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Ürün fiyatı 0'dan küçük olamaz.");
        }
        if (product.getQuantityInStock() < 0) {
            throw new IllegalArgumentException("Başlangıç stok miktarı negatif olamaz.");
        }

        // Tüm doğrulamalar (validations) başarılıysa DAO üzerinden veritabanına kaydet
        return productDAO.insert(product);
    }

    /**
     * Mevcut bir ürünün bilgilerini (isim, fiyat vb.) güncellerken kullanılacak iş mantığı.
     */
    public boolean updateProduct(Product product) throws IllegalArgumentException {
        // Güncelleme işlemi için de fiyatın eksiye düşürülmemesi gibi kurallar burada tekrar kontrol edilebilir
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Güncellenen fiyat 0'dan küçük olamaz.");
        }
        return productDAO.update(product);
    }

    public List<Product> getAllProducts() {
        return productDAO.getAll();
    }

    public boolean deleteProduct(int productId) throws IllegalStateException {
        // İş Kuralı 4: Hareket geçmişi olan ürün silinemez.
        // Stok miktarının 0 olması geçmişin olmadığı anlamına gelmez;
        // örneğin 100 giriş + 100 çıkış yapılmışsa net stok 0 ama kayıtlar hâlâ mevcuttur.
        int txCount = transactionDAO.countByProductId(productId);
        if (txCount > 0) {
            throw new IllegalStateException(
                "Bu ürüne ait " + txCount + " adet stok hareketi kaydı bulunduğundan silinemez. " +
                "Stok geçmişi denetim kaydı olarak saklanmaktadır."
            );
        }
        return productDAO.delete(productId);
    }

    public Product getProductById(int productId) {
        return productDAO.getById(productId);
    }
}
