package org.example.view.panels;

import org.example.controller.SupplierController;
import org.example.model.entity.Supplier;
import org.example.util.InventorTheme;
import org.example.util.TableHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupplierManagementPanel extends BaseManagementPanel {

    private final SupplierController supplierController;
    private JTextField txtName, txtEmail, txtPhone, txtAddress;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public SupplierManagementPanel() {
        this.supplierController = new SupplierController();
        initComponents();
        loadData();
    }

    private void initComponents() {
        // İsim=1, Email=2, Telefon=3 sütunlarında ara
        add(buildHeaderBar("TEDARİKÇİ YÖNETİMİ", 1, 2, 3), BorderLayout.NORTH);

        String[] cols = {"ID", "İsim", "E-posta", "Telefon", "Adres"};
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

        txtName    = pxField("Şirket / kişi adı");
        txtEmail   = pxField("ornek@mail.com");
        txtPhone   = pxField("05xx xxx xx xx");
        txtAddress = pxField("İl, ilçe, adres");

        JPanel formGrid = buildFormGrid();
        addFormRow(formGrid, 0, "İSİM",    txtName,  "E-POSTA",  txtEmail);
        addFormRow(formGrid, 1, "TELEFON", txtPhone, "ADRES",    txtAddress);

        btnAdd    = InventorTheme.addButton("EKLE");
        btnUpdate = InventorTheme.updateButton("GÜNCELLE");
        btnDelete = InventorTheme.deleteButton("SİL");
        btnClear  = InventorTheme.clearButton("TEMİZLE");

        add(buildFormWrapper("TEDARİKÇİ BİLGİLERİ", formGrid,
                buildButtonRow(btnAdd, btnUpdate, btnDelete, btnClear)), BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    private void handleAdd() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) { showMsg("⚠  İsim boş olamaz.", InventorTheme.COL_YELLOW); return; }
        if (supplierController.addSupplier(name,
                txtEmail.getText().trim(), txtPhone.getText().trim(), txtAddress.getText().trim())) {
            showMsg("✔  Tedarikçi eklendi.", InventorTheme.COL_GREEN);
            loadData(); clearForm();
        } else {
            showMsg("✕  Eklenemedi.", InventorTheme.COL_RED);
        }
    }

    private void handleUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) { showMsg("⚠  Güncellenecek satırı seçin.", InventorTheme.COL_YELLOW); return; }
        int m  = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(m, 0);
        String name = txtName.getText().trim();
        if (name.isEmpty()) { showMsg("⚠  İsim boş olamaz.", InventorTheme.COL_YELLOW); return; }
        if (supplierController.updateSupplier(id, name,
                txtEmail.getText().trim(), txtPhone.getText().trim(), txtAddress.getText().trim())) {
            showMsg("✔  Tedarikçi güncellendi.", InventorTheme.COL_BLUE);
            loadData(); clearForm();
        } else {
            showMsg("✕  Güncelleme başarısız.", InventorTheme.COL_RED);
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int m    = table.convertRowIndexToModel(row);
        int id   = (int) tableModel.getValueAt(m, 0);
        String n = tableModel.getValueAt(m, 1).toString();
        if (confirmDialog(n + " tedarikçisi silinsin mi?")) {
            try {
                if (supplierController.deleteSupplier(id)) {
                    showMsg("✔  Tedarikçi silindi.", InventorTheme.COL_RED);
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
            txtEmail.setText(tableModel.getValueAt(m, 2).toString());
            txtPhone.setText(tableModel.getValueAt(m, 3).toString());
            txtAddress.setText(tableModel.getValueAt(m, 4).toString());
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Supplier> list = supplierController.getAllSuppliers();
        for (Supplier s : list)
            tableModel.addRow(new Object[]{
                    s.getId(), s.getName(), s.getContactEmail(), s.getPhone(), s.getAddress()
            });
    }

    private void clearForm() {
        txtName.setText(""); txtEmail.setText("");
        txtPhone.setText(""); txtAddress.setText("");
        table.clearSelection();
    }
}