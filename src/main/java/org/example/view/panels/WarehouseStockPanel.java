package org.example.view.panels;

import org.example.controller.TransactionController;
import org.example.controller.WarehouseController;
import org.example.model.entity.Warehouse;
import org.example.util.InventorTheme;
import org.example.util.TableHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Depo bazlı anlık stok durumunu gösterir.
 * Veriler transaction geçmişinden hesaplanır (IN/OUT/TRANSFER).
 */
public class WarehouseStockPanel extends BaseManagementPanel {

    private final TransactionController transactionController;
    private final WarehouseController   warehouseController;
    private JComboBox<String>           comboFilterWarehouse;

    public WarehouseStockPanel() {
        this.transactionController = new TransactionController();
        this.warehouseController   = new WarehouseController();
        initComponents();
        loadData();
    }

    private void initComponents() {
        add(buildHeaderBar("DEPO STOK DURUMU", 0, 1, 2), BorderLayout.NORTH);

        // ── Tablo ──────────────────────────────────────────────────────────
        String[] cols = {"Depo", "SKU", "Ürün Adı", "Stok"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 3 ? Integer.class : String.class;
            }
        };
        table = new JTable(tableModel);
        TableHelper.setupTable(table);
        setupSorter();

        // Sütun 0 = Depo adı (String) — IdCellRenderer'ın %04d formatını geçersiz kıl
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(InventorTheme.vt(16f));
                setForeground(sel ? InventorTheme.ACCENT : InventorTheme.TEXT_PRIMARY);
                setBackground(sel ? new Color(0x00FFCC22, true) : InventorTheme.BG_PRIMARY);
                return this;
            }
        });

        // Stok sütununu (3) sağa hizala
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(right);

        // ── Depo Filtresi ──────────────────────────────────────────────────
        JPanel filterBar = buildFilterBar();

        JPanel tableArea = new JPanel(new BorderLayout(0, 6));
        tableArea.setBackground(InventorTheme.BG_PRIMARY);
        tableArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tableArea.add(filterBar,              BorderLayout.NORTH);
        tableArea.add(buildTableScrollPane(), BorderLayout.CENTER);
        add(tableArea, BorderLayout.CENTER);

        // ── Yenile Butonu ──────────────────────────────────────────────────
        JButton btnRefresh = InventorTheme.addButton("YENİLE");
        btnRefresh.addActionListener(e -> loadData());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        bottom.setBackground(InventorTheme.BG_SURFACE);
        bottom.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, InventorTheme.BORDER));
        bottom.add(btnRefresh);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(InventorTheme.BG_PRIMARY);

        JLabel lbl = new JLabel("DEPO FİLTRESİ:");
        lbl.setFont(InventorTheme.pixel(7f));
        lbl.setForeground(InventorTheme.COL_MUTED);

        // "TÜMÜ" + her depo adı
        List<Warehouse> warehouses = warehouseController.getAllWarehouses();
        String[] options = new String[warehouses.size() + 1];
        options[0] = "TÜMÜ";
        for (int i = 0; i < warehouses.size(); i++)
            options[i + 1] = warehouses.get(i).getName();

        comboFilterWarehouse = new JComboBox<>(options);
        comboFilterWarehouse.setFont(InventorTheme.vt(16f));
        comboFilterWarehouse.setForeground(InventorTheme.TEXT_PRIMARY);
        comboFilterWarehouse.setBackground(InventorTheme.BG_PRIMARY);
        comboFilterWarehouse.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 2));
        comboFilterWarehouse.setPreferredSize(new Dimension(200, 34));
        comboFilterWarehouse.addActionListener(e -> applyFilter());

        bar.add(lbl);
        bar.add(comboFilterWarehouse);
        return bar;
    }

    private void applyFilter() {
        if (sorter == null) return;
        String sel = (String) comboFilterWarehouse.getSelectedItem();
        if (sel == null || sel.equals("TÜMÜ")) {
            sorter.setRowFilter(null);
        } else {
            // Sütun 0 = Depo adı
            sorter.setRowFilter(RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(sel) + "$", 0));
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Object[]> report = transactionController.getWarehouseStockReport();
        for (Object[] row : report) {
            tableModel.addRow(row); // [warehouseName, sku, productName, quantity]
        }
        applyFilter(); // aktif filtre varsa koru
    }
}
