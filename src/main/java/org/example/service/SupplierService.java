package org.example.service;

import org.example.dao.impl.AssetDAO;
import org.example.dao.impl.SupplierDAO;
import org.example.model.entity.Supplier;

import java.util.List;

public class SupplierService {

    private final SupplierDAO supplierDAO;
    private final AssetDAO    assetDAO;

    public SupplierService() {
        this.supplierDAO = new SupplierDAO();
        this.assetDAO    = new AssetDAO();
    }

    /**
     * Yeni bir tedarikçi eklemeden önce iş kurallarını denetler.
     */
    public boolean addSupplier(Supplier supplier) throws IllegalArgumentException {
        // İş Kuralı 1: Firma adı zorunludur
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tedarikçi (Firma) adı boş bırakılamaz.");
        }

        // İş Kuralı 2: İletişim e-postası girilmişse formatı geçerli olmalıdır
        if (supplier.getContactEmail() != null && !supplier.getContactEmail().trim().isEmpty()) {
            if (!supplier.getContactEmail().contains("@") || !supplier.getContactEmail().contains(".")) {
                throw new IllegalArgumentException("Lütfen geçerli bir e-posta adresi giriniz (Örn: info@firma.com).");
            }
        }

        // İş Kuralı 3: Telefon numarası girilmişse uzunluk kontrolü yapılabilir
        if (supplier.getPhone() != null && supplier.getPhone().length() > 20) {
            throw new IllegalArgumentException("Telefon numarası 20 karakterden uzun olamaz.");
        }

        // Tüm kurallardan geçildiyse DAO'ya (Veritabanına) ilet
        return supplierDAO.insert(supplier);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierDAO.getAll();
    }

    public boolean updateSupplier(Supplier supplier) {
        // İş Kuralı: Email formatı kontrolü eklenebilir
        return supplierDAO.update(supplier);
    }

    public boolean deleteSupplier(int id) throws IllegalStateException {
        int assetCount = assetDAO.countBySupplierId(id);
        if (assetCount > 0) {
            throw new IllegalStateException(
                "Bu tedarikçiye ait " + assetCount +
                " adet demirbaş bulunduğu için silme işlemi gerçekleştirilemez!");
        }
        return supplierDAO.delete(id);
    }
}
