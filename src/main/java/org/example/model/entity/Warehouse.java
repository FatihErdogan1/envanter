package org.example.model.entity;

public class Warehouse {
    private int id;
    private String name;
    private String locationAddress;

    public Warehouse() {}

    public Warehouse(int id, String name, String locationAddress) {
        this.id = id;
        this.name = name;
        this.locationAddress = locationAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    @Override
    public String toString() {
        // ComboBox'ta deponun isminin (örn: Merkez Depo, Şube-1) görünmesini sağlar
        return this.name != null ? this.name : "İsimsiz Depo";
    }
}
