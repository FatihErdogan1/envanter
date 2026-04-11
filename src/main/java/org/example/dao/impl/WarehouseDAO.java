package org.example.dao.impl;

import org.example.dao.base.IGenericDao;
import org.example.model.entity.Warehouse;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO implements IGenericDao<Warehouse> {

    // Singleton DatabaseConnection kullanımı [cite: 50, 53]
    private Connection connection = DatabaseConnection.getInstance().getConnection();

    @Override
    public boolean insert(Warehouse warehouse) {
        String sql = "INSERT INTO Warehouses (name, location_address) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouse.getName());
            pstmt.setString(2, warehouse.getLocationAddress());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Depo eklenirken veritabanı hatası: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Warehouse> getAll() {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT * FROM Warehouses";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Warehouse warehouse = new Warehouse();
                warehouse.setId(rs.getInt("warehouse_id"));
                warehouse.setName(rs.getString("name"));
                warehouse.setLocationAddress(rs.getString("location_address"));

                warehouses.add(warehouse);
            }
        } catch (SQLException e) {
            System.err.println("Depolar listelenirken hata: " + e.getMessage());
        }
        return warehouses;
    }

    @Override
    public boolean update(Warehouse warehouse) {
        String sql = "UPDATE Warehouses SET name = ?, location_address = ? WHERE warehouse_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouse.getName());
            pstmt.setString(2, warehouse.getLocationAddress());
            pstmt.setInt(3, warehouse.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Depo güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Warehouses WHERE warehouse_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Depo silinirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Warehouse getById(int id) {
        String sql = "SELECT * FROM Warehouses WHERE warehouse_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Warehouse(
                            rs.getInt("warehouse_id"),
                            rs.getString("name"),
                            rs.getString("location_address")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Depo getirilirken hata: " + e.getMessage());
        }
        return null;
    }
}
