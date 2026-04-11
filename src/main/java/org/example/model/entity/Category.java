package org.example.model.entity;

public class Category {
    private int id;
    private String name;
    private String description;

    // Boş Constructor (No-args)
    public Category() {}

    // Dolu Constructor (All-args)
    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getter ve Setter metotları
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        // JComboBox'ta sadece kategori adının görünmesini sağlar
        return this.name;
    }
}

