package org.example.dao.impl;

import org.example.model.entity.AssetTransfer;
import org.example.model.entity.Warehouse;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssetTransferDAO {

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    /** Yeni bir transfer kaydı ekler ve demirbaşın depo bilgisini günceller. */
    public boolean insert(AssetTransfer transfer) {
        String sql = "INSERT INTO Asset_Transfers (asset_id, from_warehouse_id, to_warehouse_id, notes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, transfer.getAssetId());
            pstmt.setInt(2, transfer.getFromWarehouse().getId());
            pstmt.setInt(3, transfer.getToWarehouse().getId());
            pstmt.setString(4, transfer.getNotes());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Transfer kaydı oluşturulurken hata: " + e.getMessage());
            return false;
        }
    }

    /** Belirtilen demirbaşın tüm transfer geçmişini tarih azalan sırayla döner. */
    public List<AssetTransfer> getHistoryByAssetId(int assetId) {
        List<AssetTransfer> list = new ArrayList<>();
        String sql =
            "SELECT at.transfer_id, at.asset_id, at.transfer_date, at.notes, " +
            "       fw.warehouse_id AS fw_id, fw.name AS fw_name, " +
            "       tw.warehouse_id AS tw_id, tw.name AS tw_name " +
            "FROM Asset_Transfers at " +
            "JOIN Warehouses fw ON at.from_warehouse_id = fw.warehouse_id " +
            "JOIN Warehouses tw ON at.to_warehouse_id   = tw.warehouse_id " +
            "WHERE at.asset_id = ? " +
            "ORDER BY at.transfer_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, assetId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Transfer geçmişi alınırken hata: " + e.getMessage());
        }
        return list;
    }

    private AssetTransfer mapRow(ResultSet rs) throws SQLException {
        Warehouse from = new Warehouse();
        from.setId(rs.getInt("fw_id"));
        from.setName(rs.getString("fw_name"));

        Warehouse to = new Warehouse();
        to.setId(rs.getInt("tw_id"));
        to.setName(rs.getString("tw_name"));

        AssetTransfer t = new AssetTransfer();
        t.setId(rs.getInt("transfer_id"));
        t.setAssetId(rs.getInt("asset_id"));
        t.setFromWarehouse(from);
        t.setToWarehouse(to);
        t.setTransferDate(rs.getTimestamp("transfer_date").toLocalDateTime());
        t.setNotes(rs.getString("notes"));
        return t;
    }
}
