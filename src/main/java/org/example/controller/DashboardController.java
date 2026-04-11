package org.example.controller;

import org.example.service.ProductService;
import org.example.service.UserService;

public class DashboardController {
    private final ProductService productService;
    private final UserService userService;

    public DashboardController() {
        this.productService = new ProductService();
        this.userService = new UserService();
    }

    public int getTotalProductCount() {
        return productService.getAllProducts().size();
    }

    public int getLowStockCount() {
        // Stoğu 10'un altına düşen ürünleri filtreler
        return (int) productService.getAllProducts().stream()
                .filter(p -> p.getQuantityInStock() < 10)
                .count();
    }

    public int getTotalUserCount() {
        return userService.getAllUsers().size();
    }
}