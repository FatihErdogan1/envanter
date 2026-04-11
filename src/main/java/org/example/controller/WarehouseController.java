package org.example.controller;

import org.example.model.entity.Warehouse;
import org.example.service.WarehouseService;

import java.util.List;

public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController() {
        this.warehouseService = new WarehouseService();
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseService.getAllWarehouses();
    }

    public boolean addWarehouse(String name, String address) throws IllegalArgumentException {
        Warehouse warehouse = new Warehouse(0, name, address);
        return warehouseService.addWarehouse(warehouse);
    }

    public boolean updateWarehouse(int id, String name, String address) {
        Warehouse warehouse = new Warehouse(id, name, address);
        return warehouseService.updateWarehouse(warehouse);
    }

    public boolean deleteWarehouse(int id) {
        return warehouseService.deleteWarehouse(id);
    }
}
