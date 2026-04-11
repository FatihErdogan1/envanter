package org.example.model.entity;

public class Product {
    // 1. Kapsülleme (Encapsulation): Tüm alanlar private
    private int id;
    private Category category; // Veritabanındaki Foreign Key mantığının OOP karşılığı: Nesnenin kendisini tutmak
    private String sku;
    private String name;
    private double price;
    private int quantityInStock;

    // 2. Private Constructor: Dışarıdan "new Product()" kullanımını engeller,
    // nesne üretimini sadece Builder sınıfına emanet ederiz.
    private Product(ProductBuilder builder) {
        this.id = builder.id;
        this.category = builder.category;
        this.sku = builder.sku;
        this.name = builder.name;
        this.price = builder.price;
        this.quantityInStock = builder.quantityInStock;
    }

    // KRİTİK: toString() burada olmalı ki UI "name" değerini gösterebilsin
    @Override
    public String toString() {
        return this.name != null ? this.name : "İsimsiz Ürün";
    }

    public int getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    // 3. Statik Inner Class: Builder Sınıfı
    public static class ProductBuilder {
        private int id;
        private Category category;
        private String sku;
        private String name;
        private double price;
        private int quantityInStock;

        // Zorunlu alanları Builder'ın constructor'ında isteyebiliriz
        public ProductBuilder(String sku, String name) {
            this.sku = sku;
            this.name = name;
        }

        // Zincirleme (Chaining) metotlar için dönüş tipi ProductBuilder'dır
        public ProductBuilder id(int id) {
            this.id = id;
            return this;
        }

        public ProductBuilder category(Category category) {
            this.category = category;
            return this;
        }

        public ProductBuilder price(double price) {
            this.price = price;
            return this;
        }

        public ProductBuilder quantityInStock(int quantityInStock) {
            this.quantityInStock = quantityInStock;
            return this;
        }

        // Nesneyi nihai olarak üreten build() metodu
        public Product build() {
            return new Product(this);
        }
    }
}
