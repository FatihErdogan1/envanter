package org.example.dao.impl;

import org.example.dao.base.IGenericDao;
import org.example.model.entity.Asset;
import org.example.model.entity.Supplier;
import org.example.model.entity.Warehouse;
import org.example.model.enums.AssetStatus;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssetDAO implements IGenericDao<Asset> {

    private Connection connection = DatabaseConnection.getInstance().getConnection();

    @Override
    public boolean insert(Asset asset) {
        String sql = "INSERT INTO Assets (supplier_id, warehouse_id, serial_number, name, purchase_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, asset.getSupplier().getId());
            pstmt.setInt(2, asset.getWarehouse().getId());
            pstmt.setString(3, asset.getSerialNumber());
            pstmt.setString(4, asset.getName());
            pstmt.setDate(5, java.sql.Date.valueOf(asset.getPurchaseDate()));
            pstmt.setString(6, asset.getStatus().name());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Demirbaş eklenirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Asset> getAll() {
        List<Asset> assets = new ArrayList<>();

        // JOIN sorgusu: Demirbaş, Tedarikçi ve Depo tablolarını birleştiriyoruz
        String sql =
                "SELECT a.*, s.name AS s_name, w.name AS w_name, u.username AS assigned_to " +
                "FROM assets a " +
                "JOIN suppliers s ON a.supplier_id = s.supplier_id " +
                "JOIN warehouses w ON a.warehouse_id = w.warehouse_id " +
                "LEFT JOIN asset_assignments aa ON a.asset_id = aa.asset_id AND aa.return_date IS NULL " +
                "LEFT JOIN users u ON aa.user_id = u.user_id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // 1. Tedarikçi Nesnesini Oluştur
                Supplier s = new Supplier();
                s.setId(rs.getInt("supplier_id"));
                s.setName(rs.getString("s_name"));

                // 2. Depo Nesnesini Oluştur
                Warehouse w = new Warehouse();
                w.setId(rs.getInt("warehouse_id"));
                w.setName(rs.getString("w_name"));

                // 3. Asset (Demirbaş) Nesnesini İnşa Et
                Asset a = new Asset();
                a.setId(rs.getInt("asset_id")); // Diyagramdaki PK: asset_id
                a.setSerialNumber(rs.getString("serial_number"));
                a.setName(rs.getString("name"));
                java.sql.Date pd = rs.getDate("purchase_date");
                if (pd != null) a.setPurchaseDate(pd.toLocalDate());
                a.setStatus(AssetStatus.valueOf(rs.getString("status"))); // Enum dönüşümü

                // İlişkili nesneleri sete ekle
                a.setSupplier(s);
                a.setWarehouse(w);
                a.setAssignedToUsername(rs.getString("assigned_to")); // null → zimmetli değil

                assets.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Demirbaşlar listelenirken hata: " + e.getMessage());
        }
        return assets;
    }

    @Override
    public boolean update(Asset asset) {
        String sql = "UPDATE Assets SET supplier_id = ?, warehouse_id = ?, serial_number = ?, name = ?, purchase_date = ?, status = ? WHERE asset_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, asset.getSupplier().getId());
            pstmt.setInt(2, asset.getWarehouse().getId());
            pstmt.setString(3, asset.getSerialNumber());
            pstmt.setString(4, asset.getName());
            pstmt.setDate(5, java.sql.Date.valueOf(asset.getPurchaseDate()));
            pstmt.setString(6, asset.getStatus().name());
            pstmt.setInt(7, asset.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Demirbaş güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Assets WHERE asset_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Demirbaş silinirken hata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Her depo için durum bazlı demirbaş sayısını döner.
     * Her satır: [warehouseName (String), status (String), count (int)]
     */
    public List<Object[]> getStatusSummaryByWarehouse() {
        List<Object[]> result = new ArrayList<>();
        String sql =
            "SELECT w.name AS warehouse_name, a.status, COUNT(*) AS cnt " +
            "FROM Assets a " +
            "JOIN Warehouses w ON a.warehouse_id = w.warehouse_id " +
            "GROUP BY w.warehouse_id, w.name, a.status " +
            "ORDER BY w.name, a.status";
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Object[]{
                    rs.getString("warehouse_name"),
                    rs.getString("status"),
                    rs.getInt("cnt")
                });
            }
        } catch (SQLException e) {
            System.err.println("Depo bazlı demirbaş özeti alınırken hata: " + e.getMessage());
        }
        return result;
    }

    /** Yalnızca warehouse_id alanını günceller — transfer işlemlerinde kullanılır. */
    public boolean updateWarehouseId(int assetId, int warehouseId) {
        String sql = "UPDATE Assets SET warehouse_id = ? WHERE asset_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            pstmt.setInt(2, assetId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Demirbaş deposu güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    /** Yalnızca status alanını günceller — assign/return/retire işlemlerinde kullanılır. */
    public boolean updateStatus(int assetId, AssetStatus status) {
        String sql = "UPDATE Assets SET status = ? WHERE asset_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, assetId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Demirbaş durumu güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    /** Belirtilen depoya atanmış kaç adet demirbaş olduğunu döner. */
    public int countByWarehouseId(int warehouseId) {
        String sql = "SELECT COUNT(*) FROM Assets WHERE warehouse_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Depo demirbaş sayısı kontrol edilirken hata: " + e.getMessage());
        }
        return 0;
    }

    /** Belirtilen tedarikçiye bağlı kaç adet demirbaş olduğunu döner. */
    public int countBySupplierId(int supplierId) {
        String sql = "SELECT COUNT(*) FROM Assets WHERE supplier_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Tedarikçi demirbaş sayısı kontrol edilirken hata: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public Asset getById(int id) {
        String sql = "SELECT * FROM Assets WHERE asset_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Asset asset = new Asset();
                    asset.setId(rs.getInt("asset_id"));
                    asset.setSerialNumber(rs.getString("serial_number"));
                    asset.setName(rs.getString("name"));
                    java.sql.Date pd = rs.getDate("purchase_date");
                    if (pd != null) asset.setPurchaseDate(pd.toLocalDate());
                    asset.setStatus(org.example.model.enums.AssetStatus.valueOf(rs.getString("status")));
                    // Not: Supplier ve Warehouse nesneleri ilgili servisler üzerinden set edilmelidir.
                    return asset;
                }
            }
        } catch (SQLException e) {
            System.err.println("Demirbaş getirilirken hata: " + e.getMessage());
        }
        return null;
    }
}
