package org.example.view.panels;

import org.example.controller.CategoryController;
import org.example.model.entity.Category;
import org.example.util.InventorTheme;
import org.example.util.TableHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoryManagementPanel extends BaseManagementPanel {

    private final CategoryController categoryController;
    private JTextField txtName, txtDescription;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public CategoryManagementPanel() {
        this.categoryController = new CategoryController();
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Header + arama (Kategori Adı=1, Açıklama=2 sütunlarında ara)
        add(buildHeaderBar("KATEGORİ YÖNETİMİ", 1, 2), BorderLayout.NORTH);

        // Tablo
        String[] cols = {"ID", "Kategori Adı", "Açıklama"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };
        table = new JTable(tableModel);
        TableHelper.setupTable(table);
        setupSorter(); // FIX: sorter bağla

        JPanel center = buildCenterPanel();
        center.add(buildTableScrollPane());
        add(center, java.awt.BorderLayout.CENTER);

        // Form
        txtName        = pxField("Kategori adı");
        txtDescription = pxField("Açıklama");

        JPanel formGrid = buildFormGrid();
        addFormRow(formGrid, 0, "KATEGORİ ADI", txtName, "AÇIKLAMA", txtDescription);

        btnAdd    = InventorTheme.addButton("EKLE");
        btnUpdate = InventorTheme.updateButton("GÜNCELLE");
        btnDelete = InventorTheme.deleteButton("SİL");
        btnClear  = InventorTheme.clearButton("TEMİZLE");

        add(buildFormWrapper("KATEGORİ BİLGİLERİ", formGrid,
                        buildButtonRow(btnAdd, btnUpdate, btnDelete, btnClear)),
                java.awt.BorderLayout.SOUTH);

        // Olaylar
        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    private JPanel buildCenterPanel() {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setBackground(InventorTheme.BG_PRIMARY);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 12));
        return p;
    }

    private void handleAdd() {
        String name = txtName.getText().trim();
        String desc = txtDescription.getText().trim();
        if (name.isEmpty()) { showMsg("⚠  Kategori adı boş olamaz.", InventorTheme.COL_YELLOW); return; }
        if (categoryController.addCategory(name, desc)) {
            showMsg("✔  Kategori eklendi.", InventorTheme.COL_GREEN);
            loadData(); clearForm();
        } else {
            showMsg("✕  Eklenemedi. Aynı isimde kategori olabilir.", InventorTheme.COL_RED);
        }
    }

    private void handleUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) { showMsg("⚠  Güncellenecek satırı seçin.", InventorTheme.COL_YELLOW); return; }
        // FIX: view row → model row (sorter aktifken doğru index)
        int modelRow = table.convertRowIndexToModel(row);
        int id   = (int) tableModel.getValueAt(modelRow, 0);
        String name = txtName.getText().trim();
        String desc = txtDescription.getText().trim();
        if (name.isEmpty()) { showMsg("⚠  Kategori adı boş olamaz.", InventorTheme.COL_YELLOW); return; }
        if (categoryController.updateCategory(id, name, desc)) {
            showMsg("✔  Kategori güncellendi.", InventorTheme.COL_BLUE);
            loadData(); clearForm();
        } else {
            showMsg("✕  Güncelleme başarısız.", InventorTheme.COL_RED);
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { showMsg("⚠  Silinecek satırı seçin.", InventorTheme.COL_YELLOW); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int id   = (int) tableModel.getValueAt(modelRow, 0);
        String name = tableModel.getValueAt(modelRow, 1).toString();
        if (confirmDialog(name + " silinsin mi?")) {
            try {
                if (categoryController.deleteCategory(id)) {
                    showMsg("✔  Kategori silindi.", InventorTheme.COL_RED);
                    loadData(); clearForm();
                }
            } catch (IllegalStateException ex) {
                showMsg("⚠  " + ex.getMessage(), InventorTheme.COL_YELLOW);
            }
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row != -1) {
            int modelRow = table.convertRowIndexToModel(row);
            txtName.setText(tableModel.getValueAt(modelRow, 1).toString());
            txtDescription.setText(tableModel.getValueAt(modelRow, 2).toString());
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Category> list = categoryController.getAllCategories();
        for (Category c : list)
            tableModel.addRow(new Object[]{c.getId(), c.getName(), c.getDescription()});
    }

    private void clearForm() {
        txtName.setText("");
        txtDescription.setText("");
        table.clearSelection();
    }
}