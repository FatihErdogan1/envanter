package org.example.dao.impl;

import org.example.model.entity.AssetMaintenance;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssetMaintenanceDAO {

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    /** Yeni bir bakım kaydı başlatır (end_date = NULL). */
    public boolean insert(AssetMaintenance maintenance) {
        String sql = "INSERT INTO Asset_Maintenance (asset_id, description, notes) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, maintenance.getAssetId());
            pstmt.setString(2, maintenance.getDescription());
            pstmt.setString(3, maintenance.getNotes());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Bakım kaydı oluşturulurken hata: " + e.getMessage());
            return false;
        }
    }

    /** Demirbaşın açık bakım kaydını kapatır (end_date = NOW()). */
    public boolean closeActive(int assetId) {
        String sql = "UPDATE Asset_Maintenance SET end_date = NOW() WHERE asset_id = ? AND end_date IS NULL";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, assetId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Bakım kaydı kapatılırken hata: " + e.getMessage());
            return false;
        }
    }

    /** Demirbaşın aktif (açık) bakım kaydını döner; yoksa null. */
    public AssetMaintenance getActiveByAssetId(int assetId) {
        String sql = "SELECT * FROM Asset_Maintenance WHERE asset_id = ? AND end_date IS NULL LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, assetId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Aktif bakım kaydı sorgulanırken hata: " + e.getMessage());
        }
        return null;
    }

    /** Demirbaşın tüm bakım geçmişini başlangıç tarihi azalan sırayla döner. */
    public List<AssetMaintenance> getHistoryByAssetId(int assetId) {
        List<AssetMaintenance> list = new ArrayList<>();
        String sql = "SELECT * FROM Asset_Maintenance WHERE asset_id = ? ORDER BY start_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, assetId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Bakım geçmişi alınırken hata: " + e.getMessage());
        }
        return list;
    }

    private AssetMaintenance mapRow(ResultSet rs) throws SQLException {
        AssetMaintenance m = new AssetMaintenance();
        m.setId(rs.getInt("maintenance_id"));
        m.setAssetId(rs.getInt("asset_id"));
        m.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        Timestamp end = rs.getTimestamp("end_date");
        if (end != null) m.setEndDate(end.toLocalDateTime());
        m.setDescription(rs.getString("description"));
        m.setNotes(rs.getString("notes"));
        return m;
    }
}
