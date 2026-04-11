package org.example.dao.impl;

import org.example.dao.base.IGenericDao;
import org.example.model.entity.Supplier;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO implements IGenericDao<Supplier> {

    private Connection connection = DatabaseConnection.getInstance().getConnection();

    @Override
    public boolean insert(Supplier supplier) {
        String sql = "INSERT INTO Suppliers (name, contact_email, phone, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactEmail());
            pstmt.setString(3, supplier.getPhone());
            pstmt.setString(4, supplier.getAddress());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Tedarikçi eklenirken veritabanı hatası: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Supplier> getAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM Suppliers";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Supplier supplier = new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact_email"),
                        rs.getString("phone"),
                        rs.getString("address")
                );
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            System.err.println("Tedarikçiler listelenirken hata: " + e.getMessage());
        }
        return suppliers;
    }

    @Override
    public boolean update(Supplier supplier) {
        String sql = "UPDATE Suppliers SET name = ?, contact_email = ?, phone = ?, address = ? WHERE supplier_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactEmail());
            pstmt.setString(3, supplier.getPhone());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setInt(5, supplier.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Tedarikçi güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Suppliers WHERE supplier_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Tedarikçi silinirken hata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Supplier getById(int id) {
        String sql = "SELECT * FROM Suppliers WHERE supplier_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Supplier(
                            rs.getInt("supplier_id"),
                            rs.getString("name"),
                            rs.getString("contact_email"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Tedarikçi getirilirken hata: " + e.getMessage());
        }
        return null;
    }
}