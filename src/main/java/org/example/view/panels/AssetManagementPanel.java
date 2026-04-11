package org.example.view.panels;

import org.example.controller.AssetController;
import org.example.controller.SupplierController;
import org.example.controller.UserController;
import org.example.controller.WarehouseController;
import org.example.model.entity.Asset;
import org.example.model.entity.AssetAssignment;
import org.example.model.entity.AssetMaintenance;
import org.example.model.entity.AssetTransfer;
import org.example.model.entity.Supplier;
import org.example.model.entity.User;
import org.example.model.entity.Warehouse;
import org.example.model.enums.AssetStatus;
import org.example.util.InventorTheme;
import org.example.util.TableHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AssetManagementPanel extends BaseManagementPanel {

    private final AssetController     assetController;
    private final SupplierController  supplierController;
    private final WarehouseController warehouseController;
    private final UserController      userController;

    private JTextField txtSerialNumber, txtName, txtPurchaseDate;
    private JComboBox<Supplier>    comboSupplier;
    private JComboBox<Warehouse>   comboWarehouse;
    private JComboBox<AssetStatus> comboStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRetire;
    private JButton btnAssign, btnReturn, btnHistory;
    private JButton btnTransfer, btnStartMaintenance, btnEndMaintenance;

    // Stat etiketleri
    private JLabel lblTotal, lblActive, lblMaintenance, lblRetired;

    // Depo özeti şeridi — loadData'da içeriği yenilenir
    private JPanel warehouseCardStrip;

    public AssetManagementPanel() {
        this.assetController     = new AssetController();
        this.supplierController  = new SupplierController();
        this.warehouseController = new WarehouseController();
        this.userController      = new UserController();
        initComponents();
        loadData();
    }

    private void initComponents() {
        // ── Kuzey: stat barı + depo özeti şeridi ────────────────────────────
        warehouseCardStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        warehouseCardStrip.setBackground(InventorTheme.BG_PRIMARY);

        JScrollPane stripScroll = new JScrollPane(warehouseCardStrip,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        stripScroll.setBackground(InventorTheme.BG_PRIMARY);
        stripScroll.getViewport().setBackground(InventorTheme.BG_PRIMARY);
        stripScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, InventorTheme.BORDER));
        stripScroll.setPreferredSize(new Dimension(0, 90));

        JPanel northArea = new JPanel(new BorderLayout());
        northArea.setBackground(InventorTheme.BG_PRIMARY);
        northArea.add(buildStatBar(), BorderLayout.NORTH);
        northArea.add(stripScroll,   BorderLayout.CENTER);
        add(northArea, BorderLayout.NORTH);

        // Header arama barı + tablo
        JPanel tableWrapper = new JPanel(new BorderLayout(0, 0));
        tableWrapper.setBackground(InventorTheme.BG_PRIMARY);
        tableWrapper.add(buildSearchHeader(), BorderLayout.NORTH);
        tableWrapper.add(buildTable(),        BorderLayout.CENTER);
        add(tableWrapper, BorderLayout.CENTER);

        // Form
        add(buildForm(), BorderLayout.SOUTH);

        // Olaylar
        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
        btnRetire.addActionListener(e -> handleRetire());
        btnAssign.addActionListener(e -> handleAssign());
        btnReturn.addActionListener(e -> handleReturn());
        btnTransfer.addActionListener(e -> handleTransfer());
        btnStartMaintenance.addActionListener(e -> handleStartMaintenance());
        btnEndMaintenance.addActionListener(e -> handleEndMaintenance());
        btnHistory.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int m  = table.convertRowIndexToModel(row);
            int id   = (int) tableModel.getValueAt(m, 0);
            String nm = tableModel.getValueAt(m, 2).toString();
            showHistoryDialog(id, nm);
        });
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    // ── Stat Barı ─────────────────────────────────────────────────────────────

    private JPanel buildStatBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 0, 0));
        bar.setBackground(InventorTheme.BG_SURFACE);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, InventorTheme.ACCENT));

        lblTotal       = new JLabel("0", SwingConstants.CENTER);
        lblActive      = new JLabel("0", SwingConstants.CENTER);
        lblMaintenance = new JLabel("0", SwingConstants.CENTER);
        lblRetired     = new JLabel("0", SwingConstants.CENTER);

        bar.add(statCell("🖥", lblTotal,       "TOPLAM",  InventorTheme.ACCENT));
        bar.add(statCell("✔", lblActive,      "AKTİF",   InventorTheme.COL_GREEN));
        bar.add(statCell("⚙", lblMaintenance, "BAKIM",   InventorTheme.COL_YELLOW));
        bar.add(statCell("✕", lblRetired,     "HURDA",   InventorTheme.COL_RED));
        return bar;
    }

    private JPanel statCell(String icon, JLabel numLbl, String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        p.setBackground(InventorTheme.BG_SURFACE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, InventorTheme.BORDER));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLbl.setForeground(color);

        numLbl.setFont(InventorTheme.pixel(16f));
        numLbl.setForeground(color);

        JLabel subLbl = new JLabel(text);
        subLbl.setFont(InventorTheme.pixel(6f));
        subLbl.setForeground(InventorTheme.COL_MUTED);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(InventorTheme.BG_SURFACE);
        info.add(numLbl);
        info.add(subLbl);

        p.add(iconLbl);
        p.add(info);
        return p;
    }

    // ── Arama Header ─────────────────────────────────────────────────────────

    private JPanel buildSearchHeader() {
        // FIX: getLayoutComponent kaldırıldı — güvenilmez.
        // Header bar doğrudan burada oluşturuluyor, durum filtresi EAST'e ekleniyor.
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(InventorTheme.BG_SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, InventorTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel lbl = new JLabel("▶  DEMİRBAŞ YÖNETİMİ");
        lbl.setFont(InventorTheme.pixel(9f));
        lbl.setForeground(InventorTheme.ACCENT);
        bar.add(lbl, BorderLayout.WEST);

        // Sağ taraf: durum filtresi + ara kutusu + temizle
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setBackground(InventorTheme.BG_SURFACE);

        // Durum filtresi
        JComboBox<String> statusFilter = new JComboBox<>();
        statusFilter.setFont(InventorTheme.vt(15f));
        statusFilter.setForeground(InventorTheme.TEXT_PRIMARY);
        statusFilter.setBackground(InventorTheme.BG_PRIMARY);
        statusFilter.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 2));
        statusFilter.setPreferredSize(new Dimension(150, 36));
        statusFilter.addItem("TÜM DURUMLAR");
        for (AssetStatus s : AssetStatus.values())
            statusFilter.addItem(s.name());

        statusFilter.addActionListener(e -> {
            if (sorter == null) return;
            String sel = (String) statusFilter.getSelectedItem();
            if (sel == null || sel.equals("TÜM DURUMLAR")) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)^" + sel + "$", 7));
            }
        });

        // Arama alanı
        JTextField searchField = InventorTheme.searchField("Ara...");
        searchField.setPreferredSize(new Dimension(200, 36));

        JButton btnClear = InventorTheme.clearButton("✕");

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void doFilter() {
                String text = searchField.getText().trim();
                if (sorter == null) return;
                try {
                    if (text.isEmpty()) sorter.setRowFilter(null);
                    else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 3));
                } catch (java.util.regex.PatternSyntaxException ignored) {}
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { doFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { doFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doFilter(); }
        });

        btnClear.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            if (sorter != null) sorter.setRowFilter(null);
        });

        rightPanel.add(statusFilter);
        rightPanel.add(searchField);
        rightPanel.add(btnClear);
        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    // ── Tablo ────────────────────────────────────────────────────────────────

    private JPanel buildTable() {
        String[] cols = {"ID", "Seri No", "Demirbaş Adı", "Tedarikçi", "Depo", "Tarih", "Zimmetli Kişi", "Durum"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0 -> Integer.class;
                    case 5 -> java.time.LocalDate.class;
                    default -> String.class;
                };
            }
        };
        table = new JTable(tableModel);
        TableHelper.setupTable(table);
        setupSorter();

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(InventorTheme.BG_PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        p.add(buildTableScrollPane());
        return p;
    }

    // ── Form ─────────────────────────────────────────────────────────────────

    private JPanel buildForm() {
        txtSerialNumber = pxField("SN-12345");
        txtName         = pxField("Ürün adı");
        txtPurchaseDate = pxField("YYYY-AA-GG");

        comboSupplier  = pxCombo(); fillSupplierCombo();
        comboWarehouse = pxCombo(); fillWarehouseCombo();
        comboStatus    = new JComboBox<>(AssetStatus.values());
        comboStatus.setFont(InventorTheme.vt(16f));
        comboStatus.setForeground(InventorTheme.TEXT_PRIMARY);
        comboStatus.setBackground(InventorTheme.BG_PRIMARY);
        comboStatus.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 2));

        JPanel formGrid = buildFormGrid();
        addFormRow(formGrid, 0, "SERİ NUMARASI",     txtSerialNumber, "DEMİRBAŞ ADI",    txtName);
        addFormRow(formGrid, 1, "TEDARİKÇİ",         comboSupplier,  "SATIN ALIM TARİHİ", txtPurchaseDate);
        addFormRow(formGrid, 2, "DEPO",              comboWarehouse, "DURUM",             comboStatus);

        btnAdd    = InventorTheme.addButton("EKLE");
        btnUpdate = InventorTheme.updateButton("GÜNCELLE");
        btnDelete = InventorTheme.deleteButton("SİL");
        btnClear  = InventorTheme.clearButton("TEMİZLE");
        btnRetire = InventorTheme.retireButton("HURDAYA AYIR");

        btnAssign  = InventorTheme.addButton("ZİMMETLE");
        btnReturn  = InventorTheme.updateButton("ZİMMETİ DÜŞÜR");
        btnHistory = InventorTheme.searchButton("GEÇMİŞ");

        btnTransfer          = InventorTheme.updateButton("TRANSFER");
        btnStartMaintenance  = InventorTheme.retireButton("BAKIMA AL");
        btnEndMaintenance    = InventorTheme.addButton("BAKIMI TAMAMLA");

        // Başlangıçta seçili satır yok — devre dışı
        btnAssign.setEnabled(false);
        btnReturn.setEnabled(false);
        btnHistory.setEnabled(false);
        btnTransfer.setEnabled(false);
        btnStartMaintenance.setEnabled(false);
        btnEndMaintenance.setEnabled(false);

        JPanel combinedButtons = new JPanel();
        combinedButtons.setLayout(new BoxLayout(combinedButtons, BoxLayout.Y_AXIS));
        combinedButtons.setBackground(InventorTheme.BG_SURFACE);
        combinedButtons.add(buildButtonRow(btnAdd, btnUpdate, btnDelete, btnClear, btnRetire));
        combinedButtons.add(buildButtonRow(btnAssign, btnReturn, btnTransfer, btnStartMaintenance, btnEndMaintenance, btnHistory));

        return buildFormWrapper("DEMİRBAŞ DETAYLARI", formGrid, combinedButtons);
    }

    // ── Veri ─────────────────────────────────────────────────────────────────

    private void loadData() {
        tableModel.setRowCount(0);
        List<Asset> list = assetController.getAllAssets();

        int total = 0, active = 0, maint = 0, retired = 0;
        for (Asset a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getSerialNumber(),
                    a.getName(),
                    a.getSupplier()  != null ? a.getSupplier().getName()  : "-",
                    a.getWarehouse() != null ? a.getWarehouse().getName() : "-",
                    a.getPurchaseDate(),
                    a.getAssignedToUsername() != null ? a.getAssignedToUsername() : "-",
                    a.getStatus().name()
            });
            total++;
            // Enum değerini direkt nesne üzerinden karşılaştır — string eşleşme hatası yok
            AssetStatus s = a.getStatus();
            if (isActiveStatus(s))      active++;
            else if (isMaintenanceStatus(s)) maint++;
            else if (isRetiredStatus(s))    retired++;
        }
        lblTotal.setText(String.valueOf(total));
        lblActive.setText(String.valueOf(active));
        lblMaintenance.setText(String.valueOf(maint));
        lblRetired.setText(String.valueOf(retired));

        refreshWarehouseCards();
    }

    /**
     * Depo bazlı durum özetini yeniden oluşturur.
     * Her depo için bir mini kart: AVAILABLE / IN_USE / MAINTENANCE / RETIRED sayıları.
     */
    private void refreshWarehouseCards() {
        warehouseCardStrip.removeAll();

        List<Object[]> summary = assetController.getWarehouseStatusSummary();

        // Düz listeyi Map<depoadı, Map<status, count>> yapısına dönüştür
        Map<String, Map<String, Integer>> byWarehouse = new LinkedHashMap<>();
        for (Object[] row : summary) {
            String wName  = (String) row[0];
            String status = (String) row[1];
            int    count  = (int)    row[2];
            byWarehouse.computeIfAbsent(wName, k -> new LinkedHashMap<>()).put(status, count);
        }

        if (byWarehouse.isEmpty()) {
            JLabel empty = new JLabel("  Henüz hiçbir depoda demirbaş kaydı yok.");
            empty.setFont(InventorTheme.pixel(7f));
            empty.setForeground(InventorTheme.COL_MUTED);
            warehouseCardStrip.add(empty);
        } else {
            for (Map.Entry<String, Map<String, Integer>> entry : byWarehouse.entrySet()) {
                warehouseCardStrip.add(buildWarehouseCard(entry.getKey(), entry.getValue()));
            }
        }

        warehouseCardStrip.revalidate();
        warehouseCardStrip.repaint();
    }

    /** Tek bir depo için özet kart oluşturur. */
    private JPanel buildWarehouseCard(String warehouseName, Map<String, Integer> counts) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(InventorTheme.BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(InventorTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // Depo adı
        JLabel title = new JLabel(warehouseName.toUpperCase());
        title.setFont(InventorTheme.pixel(7f));
        title.setForeground(InventorTheme.ACCENT2);
        card.add(title, BorderLayout.NORTH);

        // Durum sayıları — yatay 4 hücre
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statusRow.setBackground(InventorTheme.BG_SURFACE);

        statusRow.add(statusChip("AVAIL",  counts.getOrDefault("AVAILABLE",  0), InventorTheme.COL_GREEN));
        statusRow.add(statusChip("USE",    counts.getOrDefault("IN_USE",     0), InventorTheme.ACCENT));
        statusRow.add(statusChip("MAINT",  counts.getOrDefault("MAINTENANCE",0), InventorTheme.COL_YELLOW));
        statusRow.add(statusChip("HURDA",  counts.getOrDefault("RETIRED",    0), InventorTheme.COL_RED));

        card.add(statusRow, BorderLayout.CENTER);
        return card;
    }

    /** Tek bir durum için "sayı + etiket" çipi oluşturur. */
    private JPanel statusChip(String label, int count, Color color) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        chip.setBackground(InventorTheme.BG_SURFACE);

        JLabel numLbl = new JLabel(String.valueOf(count));
        numLbl.setFont(InventorTheme.pixel(13f));
        numLbl.setForeground(count > 0 ? color : InventorTheme.COL_MUTED);

        JLabel txtLbl = new JLabel(label);
        txtLbl.setFont(InventorTheme.pixel(6f));
        txtLbl.setForeground(InventorTheme.COL_MUTED);

        chip.add(numLbl);
        chip.add(txtLbl);
        return chip;
    }

    private void fillSupplierCombo() {
        comboSupplier.removeAllItems();
        supplierController.getAllSuppliers().forEach(comboSupplier::addItem);
    }

    private void fillWarehouseCombo() {
        comboWarehouse.removeAllItems();
        warehouseController.getAllWarehouses().forEach(comboWarehouse::addItem);
    }

    // ── Handler'lar ───────────────────────────────────────────────────────────

    private void handleAdd() {
        try {
            String sn   = txtSerialNumber.getText().trim();
            String name = txtName.getText().trim();
            if (sn.isEmpty() || name.isEmpty()) {
                showMsg("⚠  Seri numarası ve ad zorunludur.", InventorTheme.COL_YELLOW); return;
            }
            Supplier  s = (Supplier)  comboSupplier.getSelectedItem();
            Warehouse w = (Warehouse) comboWarehouse.getSelectedItem();
            if (assetController.addAsset(s, w, sn, name, txtPurchaseDate.getText().trim())) {
                showMsg("✔  Demirbaş kaydedildi.", InventorTheme.COL_GREEN);
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
            Supplier  s = (Supplier)  comboSupplier.getSelectedItem();
            Warehouse w = (Warehouse) comboWarehouse.getSelectedItem();
            String status = comboStatus.getSelectedItem().toString();
            if (assetController.updateAsset(id, s, w,
                    txtSerialNumber.getText().trim(), txtName.getText().trim(),
                    txtPurchaseDate.getText().trim(), status)) {
                showMsg("✔  Güncelleme başarılı.", InventorTheme.COL_BLUE);
                loadData();
            }
        } catch (Exception ex) {
            showMsg("✕  " + ex.getMessage(), InventorTheme.COL_RED);
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int m  = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(m, 0);
        if (confirmDialog("Bu demirbaşı silmek istediğinize emin misiniz?")) {
            if (assetController.deleteAsset(id)) {
                showMsg("✔  Demirbaş silindi.", InventorTheme.COL_RED);
                loadData(); clearForm();
            }
        }
    }

    private void handleRetire() {
        int row = table.getSelectedRow();
        if (row == -1) { showMsg("⚠  Hurdaya ayrılacak demirbaşı seçin.", InventorTheme.COL_YELLOW); return; }
        int m  = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(m, 0);
        // FIX: zaten RETIRED ise uyarı ver
        String currentStatus = tableModel.getValueAt(m, 7).toString();
        if ("RETIRED".equals(currentStatus)) {
            showMsg("⚠  Bu demirbaş zaten hurdada.", InventorTheme.COL_YELLOW); return;
        }
        try {
            if (assetController.retireAsset(id)) {
                showMsg("⚠  Demirbaş hurdaya ayrıldı.", InventorTheme.ACCENT2);
                loadData();
            }
        } catch (IllegalStateException ex) {
            showMsg("⚠  " + ex.getMessage(), InventorTheme.COL_YELLOW);
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row != -1) {
            int m = table.convertRowIndexToModel(row);
            txtSerialNumber.setText(tableModel.getValueAt(m, 1).toString());
            txtName.setText(tableModel.getValueAt(m, 2).toString());
            txtPurchaseDate.setText(tableModel.getValueAt(m, 5).toString());
            AssetStatus status = AssetStatus.AVAILABLE;
            try {
                status = AssetStatus.valueOf(tableModel.getValueAt(m, 7).toString());
                comboStatus.setSelectedItem(status);
            } catch (IllegalArgumentException ignored) {}

            boolean isAvailable   = status == AssetStatus.AVAILABLE;
            boolean isInUse       = status == AssetStatus.IN_USE;
            boolean isMaintenance = status == AssetStatus.MAINTENANCE;
            btnAssign.setEnabled(isAvailable);
            btnReturn.setEnabled(isInUse);
            btnTransfer.setEnabled(isAvailable);
            btnStartMaintenance.setEnabled(isAvailable);
            btnEndMaintenance.setEnabled(isMaintenance);
            btnHistory.setEnabled(true);
        } else {
            btnAssign.setEnabled(false);
            btnReturn.setEnabled(false);
            btnTransfer.setEnabled(false);
            btnStartMaintenance.setEnabled(false);
            btnEndMaintenance.setEnabled(false);
            btnHistory.setEnabled(false);
        }
    }

    private void clearForm() {
        txtSerialNumber.setText("");
        txtName.setText("");
        txtPurchaseDate.setText("");
        comboStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    private boolean isActiveStatus(AssetStatus s) {
        String n = s.name().toUpperCase();
        return n.equals("ACTIVE") || n.equals("IN_USE") || n.equals("INUSE")
                || n.equals("AVAILABLE") || n.equals("IN_SERVICE")
                || n.equals("KULLANIM") || n.equals("AKTIF");
    }

    private boolean isMaintenanceStatus(AssetStatus s) {
        String n = s.name().toUpperCase();
        return n.equals("MAINTENANCE") || n.equals("UNDER_MAINTENANCE")
                || n.equals("REPAIR") || n.equals("IN_REPAIR")
                || n.equals("SERVICING") || n.equals("BAKIM");
    }

    private boolean isRetiredStatus(AssetStatus s) {
        String n = s.name().toUpperCase();
        return n.equals("RETIRED") || n.equals("DECOMMISSIONED")
                || n.equals("SCRAPPED") || n.equals("SCRAP")
                || n.equals("DISPOSED") || n.equals("HURDA");
    }

    // ── Zimmetleme Handler'ları ────────────────────────────────────────────────

    private void handleAssign() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int m  = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(m, 0);

        // Kullanıcı seçim dialogu
        List<User> users = userController.getAllUsers();
        if (users.isEmpty()) {
            showMsg("⚠  Sistemde kayıtlı kullanıcı bulunamadı.", InventorTheme.COL_YELLOW);
            return;
        }

        JComboBox<User> userCombo = new JComboBox<>();
        userCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value == null ? "" : value.getUsername());
            if (isSelected) { lbl.setBackground(list.getSelectionBackground()); lbl.setOpaque(true); }
            return lbl;
        });
        users.forEach(userCombo::addItem);

        JTextField notesField = new JTextField();
        Object[] message = {
            "Zimmetlenecek Kişi:", userCombo,
            "Notlar (isteğe bağlı):", notesField
        };

        int result = JOptionPane.showConfirmDialog(this, message,
                "Demirbaş Zimmetleme", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        User selectedUser = (User) userCombo.getSelectedItem();
        if (selectedUser == null) return;

        try {
            assetController.assignAsset(id, selectedUser, notesField.getText().trim());
            showMsg("✔  Demirbaş " + selectedUser.getUsername() + " adlı kişiye zimmetlendi.", InventorTheme.COL_GREEN);
            loadData();
        } catch (IllegalStateException ex) {
            showMsg("⚠  " + ex.getMessage(), InventorTheme.COL_YELLOW);
        }
    }

    private void handleReturn() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int m  = table.convertRowIndexToModel(row);
        int id   = (int) tableModel.getValueAt(m, 0);
        String assigned = tableModel.getValueAt(m, 6).toString();

        if (!confirmDialog("\"" + assigned + "\" üzerindeki zimmeti düşürmek istiyor musunuz?")) return;

        try {
            assetController.returnAsset(id);
            showMsg("✔  Zimmet düşürüldü, demirbaş AVAILABLE durumuna alındı.", InventorTheme.COL_BLUE);
            loadData();
        } catch (IllegalStateException ex) {
            showMsg("⚠  " + ex.getMessage(), InventorTheme.COL_YELLOW);
        }
    }

    private void handleTransfer() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int m  = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(m, 0);
        String currentWarehouseName = tableModel.getValueAt(m, 4).toString();

        List<Warehouse> warehouses = warehouseController.getAllWarehouses();
        if (warehouses.isEmpty()) {
            showMsg("⚠  Sistemde kayıtlı depo bulunamadı.", InventorTheme.COL_YELLOW); return;
        }

        JComboBox<Warehouse> warehouseCombo = new JComboBox<>();
        warehouses.stream()
                  .filter(w -> !w.getName().equals(currentWarehouseName))
                  .forEach(warehouseCombo::addItem);

        if (warehouseCombo.getItemCount() == 0) {
            showMsg("⚠  Transfer edilebilecek başka depo yok.", InventorTheme.COL_YELLOW); return;
        }

        JTextField notesField = new JTextField();
        Object[] message = {
            "Mevcut Depo: " + currentWarehouseName,
            "Hedef Depo:", warehouseCombo,
            "Notlar:", notesField
        };

        int result = JOptionPane.showConfirmDialog(this, message,
                "Depo Transferi", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        Warehouse toWarehouse = (Warehouse) warehouseCombo.getSelectedItem();
        Warehouse fromWarehouse = warehouses.stream()
                .filter(w -> w.getName().equals(currentWarehouseName))
                .findFirst().orElse(null);
        try {
            assetController.transferAsset(id, fromWarehouse, toWarehouse, notesField.getText().trim());
            showMsg("✔  Demirbaş " + toWarehouse.getName() + " deposuna transfer edildi.", InventorTheme.COL_GREEN);
            loadData();
        } catch (IllegalStateException ex) {
            showMsg("⚠  " + ex.getMessage(), InventorTheme.COL_YELLOW);
        }
    }

    private void handleStartMaintenance() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int m  = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(m, 0);

        JTextField descField  = new JTextField();
        JTextField notesField = new JTextField();
        Object[] message = {
            "Bakım Açıklaması:", descField,
            "Notlar (isteğe bağlı):", notesField
        };

        int result = JOptionPane.showConfirmDialog(this, message,
                "Bakıma Al", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            assetController.startMaintenance(id, descField.getText().trim(), notesField.getText().trim());
            showMsg("⚠  Demirbaş bakıma alındı.", InventorTheme.COL_YELLOW);
            loadData();
        } catch (IllegalStateException ex) {
            showMsg("⚠  " + ex.getMessage(), InventorTheme.COL_YELLOW);
        }
    }

    private void handleEndMaintenance() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int m  = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(m, 0);

        if (!confirmDialog("Bakım tamamlandı olarak işaretlensin mi? Demirbaş AVAILABLE durumuna alınacak.")) return;

        try {
            assetController.endMaintenance(id);
            showMsg("✔  Bakım tamamlandı, demirbaş kullanıma hazır.", InventorTheme.COL_GREEN);
            loadData();
        } catch (IllegalStateException ex) {
            showMsg("⚠  " + ex.getMessage(), InventorTheme.COL_YELLOW);
        }
    }

    private void showHistoryDialog(int assetId, String assetName) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(InventorTheme.pixel(7f));

        // ── Zimmet Geçmişi ──────────────────────────────────────────────────
        DefaultTableModel assignModel = new DefaultTableModel(
                new String[]{"#", "Kişi", "Zimmet Tarihi", "İade Tarihi", "Notlar"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (AssetAssignment aa : assetController.getAssignmentHistory(assetId)) {
            assignModel.addRow(new Object[]{
                aa.getId(),
                aa.getUser() != null ? aa.getUser().getUsername() : "-",
                aa.getAssignedDate() != null ? aa.getAssignedDate().toLocalDate() : "-",
                aa.getReturnDate()   != null ? aa.getReturnDate().toLocalDate()   : "Aktif",
                aa.getNotes() != null ? aa.getNotes() : ""
            });
        }
        tabs.addTab("Zimmet (" + assignModel.getRowCount() + ")", buildHistoryScrollPane(assignModel));

        // ── Transfer Geçmişi ─────────────────────────────────────────────────
        DefaultTableModel transferModel = new DefaultTableModel(
                new String[]{"#", "Çıkış Deposu", "Hedef Depo", "Tarih", "Notlar"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (AssetTransfer at : assetController.getTransferHistory(assetId)) {
            transferModel.addRow(new Object[]{
                at.getId(),
                at.getFromWarehouse() != null ? at.getFromWarehouse().getName() : "-",
                at.getToWarehouse()   != null ? at.getToWarehouse().getName()   : "-",
                at.getTransferDate()  != null ? at.getTransferDate().toLocalDate() : "-",
                at.getNotes() != null ? at.getNotes() : ""
            });
        }
        tabs.addTab("Transfer (" + transferModel.getRowCount() + ")", buildHistoryScrollPane(transferModel));

        // ── Bakım Geçmişi ────────────────────────────────────────────────────
        DefaultTableModel maintModel = new DefaultTableModel(
                new String[]{"#", "Başlangıç", "Bitiş", "Açıklama", "Notlar"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (AssetMaintenance am : assetController.getMaintenanceHistory(assetId)) {
            maintModel.addRow(new Object[]{
                am.getId(),
                am.getStartDate() != null ? am.getStartDate().toLocalDate() : "-",
                am.getEndDate()   != null ? am.getEndDate().toLocalDate()   : "Devam Ediyor",
                am.getDescription() != null ? am.getDescription() : "",
                am.getNotes()       != null ? am.getNotes()       : ""
            });
        }
        tabs.addTab("Bakım (" + maintModel.getRowCount() + ")", buildHistoryScrollPane(maintModel));

        tabs.setPreferredSize(new Dimension(680, 300));
        JOptionPane.showMessageDialog(this, tabs,
                "Geçmiş — " + assetName, JOptionPane.PLAIN_MESSAGE);
    }

    private JScrollPane buildHistoryScrollPane(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(InventorTheme.vt(14f));
        t.setRowHeight(28);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(InventorTheme.pixel(7f));
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(660, 250));
        return sp;
    }
}