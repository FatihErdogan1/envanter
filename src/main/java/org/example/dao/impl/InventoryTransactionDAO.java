package org.example.dao.impl;

import org.example.dao.base.IGenericDao;
import org.example.model.entity.InventoryTransaction;
import org.example.model.entity.Product;
import org.example.model.entity.User;
import org.example.model.entity.Warehouse;
import org.example.model.enums.TransactionType;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionDAO implements IGenericDao<InventoryTransaction> {

    private Connection connection = DatabaseConnection.getInstance().getConnection();

    @Override
    public boolean insert(InventoryTransaction transaction) {
        String sql = "INSERT INTO Inventory_Transactions (product_id, warehouse_id, destination_warehouse_id, user_id, transaction_type, quantity, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, transaction.getProduct().getId());
            pstmt.setInt(2, transaction.getWarehouse().getId());
            // destination_warehouse_id yalnızca TRANSFER işlemlerinde dolu
            if (transaction.getDestinationWarehouse() != null) {
                pstmt.setInt(3, transaction.getDestinationWarehouse().getId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setInt(4, transaction.getUser().getId());
            pstmt.setString(5, transaction.getType().name());
            pstmt.setInt(6, transaction.getQuantity());
            pstmt.setString(7, transaction.getNotes());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Stok hareketi kaydedilirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(InventoryTransaction entity) {
        throw new UnsupportedOperationException("Güvenlik: Stok hareketleri güncellenemez.");
    }

    @Override
    public boolean delete(int id) {
        throw new UnsupportedOperationException("Güvenlik: Stok hareketleri silinemez.");
    }

    @Override
    public InventoryTransaction getById(int id) {
        String sql = "SELECT * FROM Inventory_Transactions WHERE transaction_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Hareket kaydı getirilirken hata: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<InventoryTransaction> getAll() {
        List<InventoryTransaction> list = new ArrayList<>();

        String sql = "SELECT t.*, p.name AS p_name, p.sku AS p_sku, " +
                "w.name AS w_name, dw.name AS dw_name, u.username AS u_username " +
                "FROM inventory_transactions t " +
                "JOIN products p ON t.product_id = p.product_id " +
                "JOIN warehouses w ON t.warehouse_id = w.warehouse_id " +
                "LEFT JOIN warehouses dw ON t.destination_warehouse_id = dw.warehouse_id " +
                "JOIN users u ON t.user_id = u.user_id " +
                "ORDER BY t.transaction_date DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // 1. Ürün Nesnesi (Builder yapına uygun olarak)
                Product p = new Product.ProductBuilder(rs.getString("p_sku"), rs.getString("p_name"))
                        .id(rs.getInt("product_id"))
                        .build();

                // 2. Kaynak Depo Nesnesi
                Warehouse w = new Warehouse();
                w.setId(rs.getInt("warehouse_id"));
                w.setName(rs.getString("w_name"));

                // 2b. Hedef Depo Nesnesi (yalnızca TRANSFER kayıtlarında dolu)
                Warehouse dw = null;
                int destId = rs.getInt("destination_warehouse_id");
                if (!rs.wasNull()) {
                    dw = new Warehouse();
                    dw.setId(destId);
                    dw.setName(rs.getString("dw_name"));
                }

                // 3. Kullanıcı Nesnesi
                User u = new User();
                u.setId(rs.getInt("user_id"));
                u.setUsername(rs.getString("u_username"));

                // 4. Hareket Nesnesi (Tüm alanlar şemadaki gibi)
                InventoryTransaction trans = new InventoryTransaction();
                trans.setId(rs.getInt("transaction_id")); // Diyagramdaki PK: transaction_id
                trans.setProduct(p);
                trans.setWarehouse(w);
                trans.setDestinationWarehouse(dw); // null for IN/OUT
                trans.setUser(u);
                trans.setType(TransactionType.valueOf(rs.getString("transaction_type")));
                trans.setQuantity(rs.getInt("quantity"));
                trans.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                trans.setNotes(rs.getString("notes")); // Diyagramdaki sütun: notes

                list.add(trans);
            }
        } catch (SQLException e) {
            System.err.println("Stok hareketleri listelenirken hata: " + e.getMessage());
        }
        return list;
    }

    /**
     * Belirtilen deponun kaynak veya hedef olarak geçtiği kaç adet hareket kaydı olduğunu döner.
     * Depo silinmeden önce bu sayının 0 olması beklenir.
     */
    public int countByWarehouseId(int warehouseId) {
        String sql = "SELECT COUNT(*) FROM Inventory_Transactions WHERE warehouse_id = ? OR destination_warehouse_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            pstmt.setInt(2, warehouseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Depo hareket sayısı kontrol edilirken hata: " + e.getMessage());
        }
        return 0;
    }

    /** Belirtilen ürüne ait kaç adet stok hareketi kaydı olduğunu döner. */
    public int countByProductId(int productId) {
        String sql = "SELECT COUNT(*) FROM Inventory_Transactions WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Ürün hareket sayısı kontrol edilirken hata: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Her (depo, ürün) çifti için transaction geçmişinden güncel stok miktarını hesaplar.
     * IN → +, OUT → -, TRANSFER kaynak → -, TRANSFER hedef → +
     * Stoku 0 veya negatife düşmüş satırlar HAVING ile elenir.
     *
     * @return Her satır: [warehouseName (String), sku (String), productName (String), quantity (int)]
     */
    public List<Object[]> getWarehouseStockReport() {
        List<Object[]> result = new ArrayList<>();
        String sql =
            "SELECT w.name AS warehouse_name, p.sku, p.name AS product_name, " +
            "  SUM(CASE " +
            "    WHEN t.transaction_type = 'IN'                                             THEN  t.quantity " +
            "    WHEN t.transaction_type = 'OUT'                                            THEN -t.quantity " +
            "    WHEN t.transaction_type = 'TRANSFER' AND t.warehouse_id             = w.warehouse_id THEN -t.quantity " +
            "    WHEN t.transaction_type = 'TRANSFER' AND t.destination_warehouse_id = w.warehouse_id THEN  t.quantity " +
            "    ELSE 0 END" +
            "  ) AS quantity_in_stock " +
            "FROM inventory_transactions t " +
            "JOIN warehouses w ON (t.warehouse_id = w.warehouse_id OR t.destination_warehouse_id = w.warehouse_id) " +
            "JOIN products p ON t.product_id = p.product_id " +
            "GROUP BY w.warehouse_id, w.name, p.product_id, p.sku, p.name " +
            "HAVING quantity_in_stock > 0 " +
            "ORDER BY w.name, p.name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Object[]{
                    rs.getString("warehouse_name"),
                    rs.getString("sku"),
                    rs.getString("product_name"),
                    rs.getInt("quantity_in_stock")
                });
            }
        } catch (SQLException e) {
            System.err.println("Depo stok raporu alınırken hata: " + e.getMessage());
        }
        return result;
    }

    private InventoryTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        InventoryTransaction t = new InventoryTransaction();
        t.setId(rs.getInt("transaction_id"));
        t.setQuantity(rs.getInt("quantity"));
        t.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
        t.setType(TransactionType.valueOf(rs.getString("transaction_type")));

        // DÜZELTME: Diyagrama göre "notes" olmalı
        t.setNotes(rs.getString("notes"));

        return t;
    }
}
