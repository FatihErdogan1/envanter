package org.example.dao.impl;

import org.example.dao.base.IGenericDao;
import org.example.model.entity.User;
import org.example.model.enums.Role;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IGenericDao<User> {

    private Connection connection = DatabaseConnection.getInstance().getConnection();

    // IGenericDAO'dan gelen standart metotlar (insert, update, delete, getAll vb.)
    @Override
    public boolean insert(User user) {
        // Kayıt olan kullanıcılar varsayılan olarak pasif (false) başlar
        String sql = "INSERT INTO Users (username, password_hash, email, role, is_active) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole().name());
            pstmt.setBoolean(5, user.isActive()); // UserController'da ne set edildiyse o gider

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Kullanıcı kaydedilirken DB hatası: " + e.getMessage());
            return false;
        }
    }
    @Override
    public boolean update(User user) {
        String sql = "UPDATE Users SET role = ?, is_active = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getRole().name());
            pstmt.setBoolean(2, user.isActive());
            pstmt.setInt(3, user.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Kullanıcı güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    // Profil alanlarini (kullanici adi + e-posta) guncellemek icin ozel metot.
    public boolean updateUsernameAndEmail(int userId, String username, String email) {
        String sql = "UPDATE Users SET username = ?, email = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Kullanici bilgileri guncellenirken hata: " + e.getMessage());
            return false;
        }
    }
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Kullanıcı silinirken DB hatası: " + e.getMessage());
            return false;
        }
    }
    @Override
    public User getById(int id) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(Role.valueOf(rs.getString("role")));
                    user.setActive(rs.getBoolean("is_active"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Kullanıcı getirilirken hata: " + e.getMessage());
        }
        return null;
    }
    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setActive(rs.getBoolean("is_active"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Kullanıcı listesi çekilirken hata: " + e.getMessage());
        }
        return users;
    }

    // KULLANICI GİRİŞİ İÇİN ÖZEL METOT (Login ekranında çağrılacak)
    public User authenticate(String username, String passwordHash) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password_hash = ? AND is_active = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(Role.valueOf(rs.getString("role")));
                    user.setActive(rs.getBoolean("is_active"));
                    return user; // Kullanıcı bulundu
                }
            }
        } catch (SQLException e) {
            System.err.println("Giriş yapılırken hata: " + e.getMessage());
        }
        return null; // Kullanıcı bulunamadı veya şifre yanlış
    }
}
