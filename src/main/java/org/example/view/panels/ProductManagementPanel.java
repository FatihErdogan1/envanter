package org.example.view.panels;

import org.example.controller.CategoryController;
import org.example.controller.ProductController;
import org.example.model.entity.Category;
import org.example.model.entity.Product;
import org.example.util.InventorTheme;
import org.example.util.TableHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductManagementPanel extends BaseManagementPanel {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductController  productController;
    private final CategoryController categoryController;
    private final boolean            filterLowStock;

    private JTextField txtSku, txtName, txtPrice, txtQuantity;
    private JComboBox<Category> comboCategory;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public ProductManagementPanel() {
        this(false);
    }

    public ProductManagementPanel(boolean filterLowStock) {
        this.productController  = new ProductController();
        this.categoryController = new CategoryController();
        this.filterLowStock     = filterLowStock;
        initComponents();
        loadData();
    }

    private void initComponents() {
        // SKU=1, Ürün Adı=2, Kategori=3 sütunlarında ara
        add(buildHeaderBar("ÜRÜN YÖNETİMİ", 1, 2, 3), BorderLayout.NORTH);

        String[] cols = {"ID", "SKU", "Ürün Adı", "Kategori", "Fiyat (₺)", "Stok"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0, 5 -> Integer.class;
                    case 4    -> Double.class;
                    default   -> String.class;
                };
            }
        };
        table = new JTable(tableModel);
        TableHelper.setupTable(table);
        setupSorter();

        // FIX: Fiyat ve Stok sütunları sağa hizalı
        javax.swing.table.DefaultTableCellRenderer rightAlign =
                new javax.swing.table.DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(5).setCellRenderer(rightAlign);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(InventorTheme.BG_PRIMARY);
        center.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        center.add(buildTableScrollPane());
        add(center, BorderLayout.CENTER);

        txtSku      = pxField("SKU-001");
        txtName     = pxField("Ürün adı");
        txtPrice    = pxField("0.00");
        txtQuantity = pxField("0");
        comboCategory = pxCombo();
        refreshCategoryCombo(); // FIX: metot adı değişti, her loadData'da yenilenebilir

        JPanel formGrid = buildFormGrid();
        addFormRow(formGrid, 0, "SKU",            txtSku,       "ÜRÜN ADI",    txtName);
        addFormRow(formGrid, 1, "KATEGORİ",       comboCategory,"FİYAT (₺)",   txtPrice);
        addFormRow(formGrid, 2, "BAŞLANGIÇ STOĞU",txtQuantity,  null,          null);

        btnAdd    = InventorTheme.addButton("EKLE");
        btnUpdate = InventorTheme.updateButton("GÜNCELLE");
        btnDelete = InventorTheme.deleteButton("SİL");
        btnClear  = InventorTheme.clearButton("TEMİZLE");

        add(buildFormWrapper("ÜRÜN BİLGİLERİ", formGrid,
                buildButtonRow(btnAdd, btnUpdate, btnDelete, btnClear)), BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    // FIX: Her açılışta kategori listesi güncel gelsin
    private void refreshCategoryCombo() {
        Object selected = comboCategory.getSelectedItem();
        comboCategory.removeAllItems();
        List<Category> cats = categoryController.getAllCategories();
        cats.forEach(comboCategory::addItem);
        // Seçili olanı koru
        if (selected != null) {
            for (int i = 0; i < comboCategory.getItemCount(); i++) {
                if (comboCategory.getItemAt(i).getId() == ((Category) selected).getId()) {
                    comboCategory.setSelectedIndex(i); break;
                }
            }
        }
    }

    private void handleAdd() {
        try {
            String sku  = txtSku.getText().trim();
            String name = txtName.getText().trim();
            if (sku.isEmpty() || name.isEmpty()) {
                showMsg("⚠  SKU ve Ürün Adı zorunludur.", InventorTheme.COL_YELLOW); return;
            }
            Category cat = (Category) comboCategory.getSelectedItem();
            if (productController.addProduct(sku, name,
                    txtPrice.getText().trim(), txtQuantity.getText().trim(), cat)) {
                showMsg("✔  Ürün eklendi.", InventorTheme.COL_GREEN);
                loadData(); clearForm();
            }
        } catch (Exception ex) {
            showMsg("✕  " + ex.getMessage(), InventorTheme.COL_RED);
        }
    }

    private void handleUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) { showMsg("⚠  Güncellenecek satırı seçin.", InventorTheme.COL_YELLOW); return; }
        try {
            int m  = table.convertRowIndexToModel(row);
            int id = (int) tableModel.getValueAt(m, 0);
            Category cat = (Category) comboCategory.getSelectedItem();
            if (productController.updateProduct(id,
                    txtSku.getText().trim(), txtName.getText().trim(),
                    txtPrice.getText().trim(), txtQuantity.getText().trim(), cat)) {
                showMsg("✔  Ürün güncellendi.", InventorTheme.COL_BLUE);
                loadData(); clearForm();
            }
        } catch (Exception ex) {
            showMsg("✕  " + ex.getMessage(), InventorTheme.COL_RED);
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { showMsg("⚠  Silinecek satırı seçin.", InventorTheme.COL_YELLOW); return; }
        int m    = table.convertRowIndexToModel(row);
        int id   = (int) tableModel.getValueAt(m, 0);
        String n = tableModel.getValueAt(m, 2).toString();
        if (confirmDialog(n + " silinsin mi?")) {
            try {
                if (productController.deleteProduct(id)) {
                    showMsg("✔  Ürün silindi.", InventorTheme.COL_RED);
                    loadData(); clearForm();
                } else {
                    showMsg("✕  Ürün silinemedi.", InventorTheme.COL_RED);
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
            txtSku.setText(tableModel.getValueAt(m, 1).toString());
            txtName.setText(tableModel.getValueAt(m, 2).toString());
            txtPrice.setText(tableModel.getValueAt(m, 4).toString());
            txtQuantity.setText(tableModel.getValueAt(m, 5).toString());
            String catName = tableModel.getValueAt(m, 3).toString();
            for (int i = 0; i < comboCategory.getItemCount(); i++) {
                if (comboCategory.getItemAt(i).getName().equals(catName)) {
                    comboCategory.setSelectedIndex(i); break;
                }
            }
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Product> list = productController.getAllProducts();
        for (Product p : list)
            tableModel.addRow(new Object[]{
                    p.getId(), p.getSku(), p.getName(),
                    p.getCategory() != null ? p.getCategory().getName() : "-",
                    p.getPrice(), p.getQuantityInStock()
            });
        // FIX: Ürün eklenince kategori listesini de tazele
        refreshCategoryCombo();
        if (filterLowStock && sorter != null) {
            sorter.setRowFilter(RowFilter.numberFilter(
                    RowFilter.ComparisonType.BEFORE, LOW_STOCK_THRESHOLD, 5));
        }
    }

    private void clearForm() {
        txtSku.setText(""); txtName.setText("");
        txtPrice.setText(""); txtQuantity.setText("");
        table.clearSelection();
    }
}