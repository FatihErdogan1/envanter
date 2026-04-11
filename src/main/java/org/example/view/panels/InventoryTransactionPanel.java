package org.example.view.panels;

import org.example.controller.ProductController;
import org.example.controller.TransactionController;
import org.example.controller.WarehouseController;
import org.example.model.entity.InventoryTransaction;
import org.example.model.entity.Product;
import org.example.model.entity.Warehouse;
import org.example.model.enums.TransactionType;
import org.example.util.InventorTheme;
import org.example.util.SessionManager;
import org.example.util.TableHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryTransactionPanel extends BaseManagementPanel {

    private final TransactionController transactionController;
    private final ProductController     productController;
    private final WarehouseController   warehouseController;

    private JComboBox<Product>         comboProduct;
    private JComboBox<Warehouse>       comboWarehouse;
    private JComboBox<Warehouse>       comboDestWarehouse; // Yalnızca TRANSFER için
    private JComboBox<TransactionType> comboType;
    private JComboBox<String>          comboFilterType; // FIX: tip filtresi
    private JTextField                 txtQuantity, txtNotes;
    private JButton                    btnSave;
    private JLabel                     lblDestWarehouse; // etiket, göster/gizle için

    public InventoryTransactionPanel() {
        this.transactionController = new TransactionController();
        this.productController     = new ProductController();
        this.warehouseController   = new WarehouseController();
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Header: Ürün=2, Depo=3 sütunlarında metin araması
        add(buildHeaderBar("STOK HAREKETLERİ", 2, 3), BorderLayout.NORTH);

        // ── Tablo ──────────────────────────────────────────────────────────
        String[] cols = {"ID", "Tarih", "Ürün", "Depo", "Tip", "Miktar", "Not"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0, 5 -> Integer.class;
                    case 1    -> java.time.LocalDateTime.class;
                    default   -> String.class;
                };
            }
        };
        table = new JTable(tableModel);
        TableHelper.setupTable(table);
        setupSorter();

        // FIX: TransactionType renderer — enum.name() ile doğru eşleşme
        table.getColumnModel().getColumn(4).setCellRenderer(new TransactionTypeRenderer());
        // Miktar sağa hizalı
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(right);

        // ── Tip Filtresi (header altı) ─────────────────────────────────────
        JPanel filterBar = buildFilterBar();

        JPanel tableArea = new JPanel(new BorderLayout(0, 6));
        tableArea.setBackground(InventorTheme.BG_PRIMARY);
        tableArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        tableArea.add(filterBar,              BorderLayout.NORTH);
        tableArea.add(buildTableScrollPane(), BorderLayout.CENTER);
        add(tableArea, BorderLayout.CENTER);

        // ── Form ───────────────────────────────────────────────────────────
        comboProduct      = pxCombo();
        comboWarehouse    = pxCombo();
        comboDestWarehouse = pxCombo();
        comboType         = new JComboBox<>(TransactionType.values());
        comboType.setFont(InventorTheme.vt(16f));
        comboType.setForeground(InventorTheme.TEXT_PRIMARY);
        comboType.setBackground(InventorTheme.BG_PRIMARY);
        comboType.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 2));

        List<Warehouse> warehouses = warehouseController.getAllWarehouses();
        productController.getAllProducts().forEach(comboProduct::addItem);
        warehouses.forEach(comboWarehouse::addItem);
        warehouses.forEach(comboDestWarehouse::addItem);

        txtQuantity = pxField("Adet");
        txtNotes    = pxField("Açıklama / Not");

        JPanel formGrid = buildFormGrid();
        addFormRow(formGrid, 0, "ÜRÜN",         comboProduct,      "KAYNAK DEPO", comboWarehouse);
        addFormRow(formGrid, 1, "İŞLEM TİPİ",   comboType,         "MİKTAR",      txtQuantity);

        // Satır 2: hedef depo — başlangıçta gizli, TRANSFER seçilince görünür
        lblDestWarehouse = new JLabel("HEDEF DEPO");
        lblDestWarehouse.setFont(InventorTheme.pixel(7f));
        lblDestWarehouse.setForeground(InventorTheme.COL_MUTED);
        addFormRow(formGrid, 2, "HEDEF DEPO",   comboDestWarehouse, "NOT / AÇIKLAMA", txtNotes);
        setDestWarehouseVisible(false);

        // Tip değişince hedef depo satırını göster/gizle
        comboType.addActionListener(e ->
                setDestWarehouseVisible(comboType.getSelectedItem() == TransactionType.TRANSFER));

        btnSave = InventorTheme.addButton("İŞLEMİ KAYDET");

        add(buildFormWrapper("YENİ STOK HAREKETİ", formGrid,
                buildButtonRow(btnSave)), BorderLayout.SOUTH);

        btnSave.addActionListener(e -> handleSave());
    }

    /** TRANSFER satırını (hedef depo) gösterir veya gizler. */
    private void setDestWarehouseVisible(boolean visible) {
        comboDestWarehouse.setVisible(visible);
        lblDestWarehouse.setVisible(visible);
    }

    // ── Tip Filtresi Paneli ───────────────────────────────────────────────────

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(InventorTheme.BG_PRIMARY);

        JLabel lbl = new JLabel("TİP FİLTRESİ:");
        lbl.setFont(InventorTheme.pixel(7f));
        lbl.setForeground(InventorTheme.COL_MUTED);

        // FIX: "TÜMÜ" + her enum değeri
        String[] options = new String[TransactionType.values().length + 1];
        options[0] = "TÜMÜ";
        for (int i = 0; i < TransactionType.values().length; i++)
            options[i + 1] = TransactionType.values()[i].name();
        comboFilterType = new JComboBox<>(options);
        comboFilterType.setFont(InventorTheme.vt(16f));
        comboFilterType.setForeground(InventorTheme.TEXT_PRIMARY);
        comboFilterType.setBackground(InventorTheme.BG_PRIMARY);
        comboFilterType.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 2));
        comboFilterType.setPreferredSize(new Dimension(160, 34));

        comboFilterType.addActionListener(e -> applyTypeFilter());

        bar.add(lbl);
        bar.add(comboFilterType);
        return bar;
    }

    // FIX: Tip filtresini sorter'a uygula
    private void applyTypeFilter() {
        if (sorter == null) return;
        String sel = (String) comboFilterType.getSelectedItem();
        if (sel == null || sel.equals("TÜMÜ")) {
            sorter.setRowFilter(null);
        } else {
            // Sütun 4 = Tip
            sorter.setRowFilter(RowFilter.regexFilter("(?i)^" + sel + "$", 4));
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<InventoryTransaction> list = transactionController.getAllTransactions();
        for (InventoryTransaction t : list) {
            String depoStr;
            if (t.getType() == TransactionType.TRANSFER && t.getDestinationWarehouse() != null) {
                depoStr = t.getWarehouse().getName() + " → " + t.getDestinationWarehouse().getName();
            } else {
                depoStr = t.getWarehouse().getName();
            }
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getTransactionDate(),
                    t.getProduct().getName(),
                    depoStr,
                    t.getType().name(),
                    t.getQuantity(),
                    t.getNotes() != null ? t.getNotes() : ""
            });
        }
    }

    private void handleSave() {
        try {
            Product   p      = (Product)   comboProduct.getSelectedItem();
            Warehouse src    = (Warehouse) comboWarehouse.getSelectedItem();
            if (p == null || src == null) {
                showMsg("⚠  Ürün ve Kaynak Depo seçimi zorunludur.", InventorTheme.COL_YELLOW); return;
            }
            String qty = txtQuantity.getText().trim();
            if (qty.isEmpty()) {
                showMsg("⚠  Miktar boş olamaz.", InventorTheme.COL_YELLOW); return;
            }

            TransactionType type = (TransactionType) comboType.getSelectedItem();
            Warehouse dest = null;
            if (type == TransactionType.TRANSFER) {
                dest = (Warehouse) comboDestWarehouse.getSelectedItem();
                if (dest == null) {
                    showMsg("⚠  Transfer işlemi için Hedef Depo seçilmelidir.", InventorTheme.COL_YELLOW); return;
                }
            }

            boolean ok = transactionController.processTransaction(
                    p, src, dest, SessionManager.getCurrentUser(),
                    type.name(), qty, txtNotes.getText().trim()
            );
            if (ok) {
                showMsg("✔  Stok hareketi kaydedildi.", InventorTheme.COL_GREEN);
                loadData();
                txtQuantity.setText(""); txtNotes.setText("");
                applyTypeFilter();
            }
        } catch (Exception ex) {
            showMsg("✕  " + ex.getMessage(), InventorTheme.COL_RED);
        }
    }

    // ── İşlem Tipi Renderer ───────────────────────────────────────────────────
    // FIX: Önceki versiyon IN/OUT içerip içermediğine bakıyordu — enum.name() kesin eşleşme

    static class TransactionTypeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            String v = val != null ? val.toString().toUpperCase() : "";

            Color c;
            if (v.equals("TRANSFER")) {
                c = InventorTheme.COL_YELLOW;
            } else if (v.contains("IN") || v.contains("PURCHASE") || v.contains("RETURN") || v.contains("GIRI")) {
                c = InventorTheme.COL_GREEN;
            } else {
                c = InventorTheme.COL_RED;
            }

            setFont(InventorTheme.pixel(7f));
            setForeground(c);
            setBackground(sel ? new Color(0x00FFCC22, true) : InventorTheme.BG_PRIMARY);
            setText("[ " + v + " ]");
            setHorizontalAlignment(CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            return this;
        }
    }
}