package org.example.dao.impl;

import org.example.model.entity.AssetAssignment;
import org.example.model.entity.User;
import org.example.model.enums.Role;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssetAssignmentDAO {

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    /** Yeni bir zimmet kaydı başlatır. */
    public boolean insert(AssetAssignment assignment) {
        String sql = "INSERT INTO Asset_Assignments (asset_id, user_id, assigned_date, notes) VALUES (?, ?, NOW(), ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, assignment.getAssetId());
            pstmt.setInt(2, assignment.getUser().getId());
            pstmt.setString(3, assignment.getNotes());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Zimmet kaydı oluşturulurken hata: " + e.getMessage());
            return false;
        }
    }

    /** Aktif zimmeti kapatır (iade işlemi). */
    public boolean closeAssignment(int assignmentId) {
        String sql = "UPDATE Asset_Assignments SET return_date = NOW() WHERE assignment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, assignmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Zimmet kapatılırken hata: " + e.getMessage());
            return false;
        }
    }

    /** Belirtilen demirbaşın aktif zimmetini döner; yoksa null. */
    public AssetAssignment getActiveByAssetId(int assetId) {
        String sql =
            "SELECT aa.*, u.username, u.email, u.role, u.is_active " +
            "FROM Asset_Assignments aa " +
            "JOIN Users u ON aa.user_id = u.user_id " +
            "WHERE aa.asset_id = ? AND aa.return_date IS NULL " +
            "LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, assetId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Aktif zimmet sorgulanırken hata: " + e.getMessage());
        }
        return null;
    }

    /** Belirtilen demirbaşın tüm zimmet geçmişini tarih azalan sırayla döner. */
    public List<AssetAssignment> getHistoryByAssetId(int assetId) {
        List<AssetAssignment> list = new ArrayList<>();
        String sql =
            "SELECT aa.*, u.username, u.email, u.role, u.is_active " +
            "FROM Asset_Assignments aa " +
            "JOIN Users u ON aa.user_id = u.user_id " +
            "WHERE aa.asset_id = ? " +
            "ORDER BY aa.assigned_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, assetId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Zimmet geçmişi alınırken hata: " + e.getMessage());
        }
        return list;
    }

    private AssetAssignment mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("is_active"));

        AssetAssignment aa = new AssetAssignment();
        aa.setId(rs.getInt("assignment_id"));
        aa.setAssetId(rs.getInt("asset_id"));
        aa.setUser(u);
        aa.setAssignedDate(rs.getTimestamp("assigned_date").toLocalDateTime());
        Timestamp ret = rs.getTimestamp("return_date");
        if (ret != null) aa.setReturnDate(ret.toLocalDateTime());
        aa.setNotes(rs.getString("notes"));
        return aa;
    }
}
