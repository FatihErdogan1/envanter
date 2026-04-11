package org.example.dao.impl;

import org.example.dao.base.IGenericDao;
import org.example.model.entity.Product;
import org.example.model.entity.Category; // Kategori ilişkisi için
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements IGenericDao<Product> {

    // Singleton bağlantımızı alıyoruz
    private Connection connection = DatabaseConnection.getInstance().getConnection();

    @Override
    public boolean insert(Product product) {
        // category_id, sku, name, price, quantity_in_stock
        String sql = "INSERT INTO Products (category_id, sku, name, price, quantity_in_stock) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Soru işaretleri (?) yerine Product nesnesinin içindeki verileri yerleştiriyoruz
            pstmt.setInt(1, product.getCategory().getId());
            pstmt.setString(2, product.getSku());
            pstmt.setString(3, product.getName());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setInt(5, product.getQuantityInStock());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Eğer 0'dan fazla satır etkilendiyse işlem başarılıdır

        } catch (SQLException e) {
            System.err.println("Ürün eklenirken veritabanı hatası: " + e.getMessage());
            return false;
        }
    }

    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();

        // DÜZELTME: ON kısmındaki c.id yerine c.category_id yazıyoruz
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category cat = new Category();
                // Burada rs.getInt("category_id") hem p tablosunda hem c tablosunda olduğu için sorun çıkarmaz
                cat.setId(rs.getInt("category_id"));
                cat.setName(rs.getString("category_name"));

                Product p = new Product.ProductBuilder(rs.getString("sku"), rs.getString("name"))
                        .id(rs.getInt("product_id")) // Önceki adımda düzeltmiştik
                        .category(cat)
                        .price(rs.getDouble("price"))
                        .quantityInStock(rs.getInt("quantity_in_stock"))
                        .build();

                products.add(p);
            }
        } catch (SQLException e) {
            // Artık burada "Unknown column 'c.id'" hatası görmeyeceksin
            System.err.println("Ürünler listelenirken SQL hatası: " + e.getMessage());
        }
        return products;
    }

    // update, delete ve getById metotları da benzer mantıkla PreparedStatement ile doldurulacaktır...
    @Override
    public boolean update(Product product) {
        String sql = "UPDATE Products SET category_id = ?, sku = ?, name = ?, price = ?, quantity_in_stock = ? WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, product.getCategory().getId());
            pstmt.setString(2, product.getSku());
            pstmt.setString(3, product.getName());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setInt(5, product.getQuantityInStock());
            pstmt.setInt(6, product.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ürün güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ürün silinirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Product getById(int id) {
        String sql = "SELECT * FROM Products WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Daha önce yazdığımız Builder Pattern kullanımı
                    return new Product.ProductBuilder(rs.getString("sku"), rs.getString("name"))
                            .id(rs.getInt("product_id"))
                            .price(rs.getDouble("price"))
                            .quantityInStock(rs.getInt("quantity_in_stock"))
                            // Kategori ataması CategoryService/DAO üzerinden yapılabilir
                            .build();
                }
            }
        } catch (SQLException e) {
            System.err.println("Ürün getirilirken hata: " + e.getMessage());
        }
        return null;
    }

    // ProductDAO.java içine eklenecek
    public int countByCategoryId(int categoryId) {
        String sql = "SELECT COUNT(*) FROM Products WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Kategori ürün sayısı kontrol edilirken hata: " + e.getMessage());
        }
        return 0;
    }
}
