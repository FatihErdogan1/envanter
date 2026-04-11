package org.example.controller;

import org.example.model.entity.Category;
import org.example.model.entity.Product;
import org.example.service.ProductService;

import java.util.List;

public class ProductController {
    private final ProductService productService;

    public ProductController() {
        this.productService = new ProductService();
    }

    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    public boolean addProduct(String sku, String name, String priceText, String quantityText, Category category) throws IllegalArgumentException {
        try {
            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(quantityText);

            Product product = new Product.ProductBuilder(sku, name)
                    .price(price)
                    .quantityInStock(quantity)
                    .category(category)
                    .build();

            return productService.addProduct(product);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Fiyat ve miktar alanlarına sadece sayı girilmelidir.");
        }
    }

    public boolean updateProduct(int id, String sku, String name, String priceText, String quantityText, Category category) throws IllegalArgumentException {
        try {
            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(quantityText);

            Product product = new Product.ProductBuilder(sku, name)
                    .id(id)
                    .price(price)
                    .quantityInStock(quantity)
                    .category(category)
                    .build();

            return productService.updateProduct(product);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Lütfen geçerli sayısal değerler giriniz.");
        }
    }

    public boolean deleteProduct(int id) {
        return productService.deleteProduct(id);
    }
}
