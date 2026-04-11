package org.example.controller;

import org.example.model.entity.Supplier;
import org.example.service.SupplierService;

import java.util.List;

public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController() {
        this.supplierService = new SupplierService();
    }

    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    public boolean addSupplier(String name, String email, String phone, String address) throws IllegalArgumentException {
        Supplier supplier = new Supplier(0, name, email, phone, address);
        return supplierService.addSupplier(supplier);
    }

    public boolean updateSupplier(int id, String name, String email, String phone, String address) {
        Supplier supplier = new Supplier(id, name, email, phone, address);
        return supplierService.updateSupplier(supplier);
    }

    public boolean deleteSupplier(int id) {
        return supplierService.deleteSupplier(id);
    }
}
