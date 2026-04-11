package org.example.dao.impl;

import org.example.dao.base.IGenericDao;
import org.example.model.entity.Category;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO implements IGenericDao<Category> {

    private Connection connection = DatabaseConnection.getInstance().getConnection();

    @Override
    public boolean insert(Category category) {
        String sql = "INSERT INTO Categories (name, description) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Kategori eklenirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Kategoriler listelenirken hata: " + e.getMessage());
        }
        return categories;
    }

    @Override
    public boolean update(Category category) {
        String sql = "UPDATE Categories SET name = ?, description = ? WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Kategori güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Categories WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Kategori silinirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Category getById(int id) {
        String sql = "SELECT * FROM Categories WHERE category_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getInt("category_id"),
                            rs.getString("name"),
                            rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Kategori getirilirken hata: " + e.getMessage());
        }
        return null;
    }
}