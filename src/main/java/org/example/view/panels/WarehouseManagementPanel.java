package org.example.view.panels;

import org.example.controller.WarehouseController;
import org.example.model.entity.Warehouse;
import org.example.util.InventorTheme;
import org.example.util.TableHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class WarehouseManagementPanel extends BaseManagementPanel {

    private final WarehouseController warehouseController;
    private JTextField txtName, txtAddress;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public WarehouseManagementPanel() {
        this.warehouseController = new WarehouseController();
        initComponents();
        loadData();
    }

    private void initComponents() {
        add(buildHeaderBar("DEPO YÖNETİMİ", 1, 2), BorderLayout.NORTH);

        String[] cols = {"ID", "Depo Adı", "Adres"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };
        table = new JTable(tableModel);
        TableHelper.setupTable(table);
        setupSorter();

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(InventorTheme.BG_PRIMARY);
        center.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        center.add(buildTableScrollPane());
        add(center, BorderLayout.CENTER);

        txtName    = pxField("Depo adı");
        txtAddress = pxField("Adres / Konum");

        JPanel formGrid = buildFormGrid();
        addFormRow(formGrid, 0, "DEPO ADI", txtName, "ADRES", txtAddress);

        btnAdd    = InventorTheme.addButton("EKLE");
        btnUpdate = InventorTheme.updateButton("GÜNCELLE");
        btnDelete = InventorTheme.deleteButton("SİL");
        btnClear  = InventorTheme.clearButton("TEMİZLE");

        add(buildFormWrapper("DEPO BİLGİLERİ", formGrid,
                buildButtonRow(btnAdd, btnUpdate, btnDelete, btnClear)), BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    private void handleAdd() {
        String name = txtName.getText().trim();
        String addr = txtAddress.getText().trim();
        if (name.isEmpty()) { showMsg("⚠  Depo adı boş olamaz.", InventorTheme.COL_YELLOW); return; }
        if (warehouseController.addWarehouse(name, addr)) {
            showMsg("✔  Depo eklendi.", InventorTheme.COL_GREEN);
            loadData(); clearForm();
        } else {
            showMsg("✕  Depo eklenemedi.", InventorTheme.COL_RED);
        }
    }

    private void handleUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) { showMsg("⚠  Güncellenecek satırı seçin.", InventorTheme.COL_YELLOW); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = txtName.getText().trim();
        if (name.isEmpty()) { showMsg("⚠  Depo adı boş olamaz.", InventorTheme.COL_YELLOW); return; }
        if (warehouseController.updateWarehouse(id, name, txtAddress.getText().trim())) {
            showMsg("✔  Depo güncellendi.", InventorTheme.COL_BLUE);
            loadData(); clearForm();
        } else {
            showMsg("✕  Güncelleme başarısız.", InventorTheme.COL_RED);
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = tableModel.getValueAt(modelRow, 1).toString();
        if (confirmDialog(name + " deposu silinsin mi?")) {
            try {
                if (warehouseController.deleteWarehouse(id)) {
                    showMsg("✔  Depo silindi.", InventorTheme.COL_RED);
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
            int m = table.convertRowIndexToModel(row);
            txtName.setText(tableModel.getValueAt(m, 1).toString());
            txtAddress.setText(tableModel.getValueAt(m, 2).toString());
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Warehouse> list = warehouseController.getAllWarehouses();
        for (Warehouse w : list)
            tableModel.addRow(new Object[]{w.getId(), w.getName(), w.getLocationAddress()});
    }

    private void clearForm() {
        txtName.setText(""); txtAddress.setText("");
        table.clearSelection();
    }
}