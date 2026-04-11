package org.example.model.enums;

public enum Role {
    ADMIN,      // Tüm sistem ayarlarına, kullanıcılara ve raporlara erişebilir
    MANAGER,    // Kategori, ürün ve depoları yönetebilir
    STAFF       // Sadece stok giriş/çıkış işlemi yapabilir, ürünleri listeleyebilir
}
