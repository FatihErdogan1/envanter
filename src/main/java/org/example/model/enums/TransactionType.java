package org.example.model.enums;

public enum TransactionType {
    IN,         // Stoka ürün girişi (Miktarı artırır)
    OUT,        // Stoktan ürün çıkışı / satışı (Miktarı azaltır)
    TRANSFER    // Depolar arası transfer (Bir depodan azaltır, diğerine ekler)
}
