package org.example.service;

import org.example.dao.impl.CategoryDAO;
import org.example.dao.impl.ProductDAO; // Kritik: Ürün kontrolü için dahil edildi
import org.example.model.entity.Category;

import java.util.List;

public class CategoryService {

    private final CategoryDAO categoryDAO;
    private final ProductDAO productDAO; // Kontrol mekanizması için eklendi

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.productDAO = new ProductDAO(); // Constructor'da initialize edildi
    }

    public boolean addCategory(Category category) throws IllegalArgumentException {
        // İş Kuralı 1: Kategori adı boş bırakılamaz
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Kategori adı boş olamaz!");
        }

        // İş Kuralı 2: İsim uzunluğu kontrolü
        if (category.getName().length() > 100) {
            throw new IllegalArgumentException("Kategori adı 100 karakterden uzun olamaz.");
        }

        return categoryDAO.insert(category);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAll();
    }

    public boolean updateCategory(Category category) {
        return categoryDAO.update(category);
    }

    /**
     * Rapor Bölüm 8: Hata Yönetimi ve Veri Bütünlüğü (Referential Integrity)
     * Bir kategoride ürün varken o kategorinin silinmesi engellenir.
     */
    public boolean deleteCategory(int categoryId) throws IllegalStateException {
        // 1. ADIM: Bu kategoriye bağlı ürün sayısını kontrol et
        // (Not: ProductDAO'da countByCategoryId metodunun yazılmış olması gerekir)
        int productCount = productDAO.countByCategoryId(categoryId);

        // 2. ADIM: İş Kuralı Denetimi
        if (productCount > 0) {
            // Eğer ürün varsa IllegalStateException fırlatarak işlemi durduruyoruz
            throw new IllegalStateException("Bu kategoriye ait " + productCount +
                    " adet ürün bulunduğu için silme işlemi gerçekleştirilemez!");
        }

        // 3. ADIM: Eğer ürün yoksa güvenle sil
        return categoryDAO.delete(categoryId);
    }
}