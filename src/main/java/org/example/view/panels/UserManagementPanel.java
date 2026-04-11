package org.example.view.panels;

import org.example.controller.UserController;
import org.example.model.entity.User;
import org.example.util.InventorTheme;
import org.example.util.TableHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends BaseManagementPanel {

    private final UserController userController;

    // Düzenleme formu
    private JTextField txtEditUsername, txtEditEmail;
    private JButton btnSaveEdit, btnCancelEdit;
    private JLabel lblEditTitle;
    private int editingUserId = -1;

    // Aksiyon butonları
    private JButton btnApprove, btnMakeManager, btnMakeStaff, btnDeactivate, btnDelete;

    public UserManagementPanel() {
        this.userController = new UserController();
        initComponents();
        loadUserData();
    }

    private void initComponents() {
        add(buildHeaderBar("KULLANICI YÖNETİMİ", 1, 2, 3), BorderLayout.NORTH);

        // ── Tablo ────────────────────────────────────────────────────────────
        String[] cols = {"ID", "Kullanıcı Adı", "E-posta", "Rol", "Durum"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };
        table = new JTable(tableModel);
        TableHelper.setupTable(table);
        setupSorter();
        table.getColumnModel().getColumn(3).setCellRenderer(new RoleRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(InventorTheme.BG_PRIMARY);
        center.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        center.add(buildTableScrollPane());
        add(center, BorderLayout.CENTER);

        // ── Alt Panel: Düzenleme Formu + Aksiyon Butonları ──────────────────
        add(buildSouthPanel(), BorderLayout.SOUTH);

        // Tablo seçimi → formu doldur
        table.getSelectionModel().addListSelectionListener(e -> fillEditForm());
    }

    // ── Alt Panel ─────────────────────────────────────────────────────────────

    private JPanel buildSouthPanel() {
        JPanel south = new JPanel(new BorderLayout(0, 0));
        south.setBackground(InventorTheme.BG_SURFACE);
        south.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, InventorTheme.BORDER));

        south.add(buildEditForm(),    BorderLayout.CENTER);
        south.add(buildActionBar(),   BorderLayout.SOUTH);
        return south;
    }

    // ── Düzenleme Formu ───────────────────────────────────────────────────────

    private JPanel buildEditForm() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(InventorTheme.BG_SURFACE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 16, 8, 16));

        lblEditTitle = new JLabel("> KULLANICI BİLGİLERİNİ DÜZENLE");
        lblEditTitle.setFont(InventorTheme.pixel(7f));
        lblEditTitle.setForeground(InventorTheme.ACCENT2);
        lblEditTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel formGrid = buildFormGrid();
        txtEditUsername = pxField("Kullanıcı adı");
        txtEditEmail    = pxField("ornek@mail.com");
        addFormRow(formGrid, 0, "KULLANICI ADI", txtEditUsername, "E-POSTA", txtEditEmail);

        btnSaveEdit   = InventorTheme.updateButton("KAYDET");
        btnCancelEdit = InventorTheme.clearButton("İPTAL");
        btnSaveEdit.setEnabled(false);
        btnCancelEdit.setEnabled(false);

        JPanel btnRow = buildButtonRow(btnSaveEdit, btnCancelEdit);

        wrapper.add(lblEditTitle, BorderLayout.NORTH);
        wrapper.add(formGrid,     BorderLayout.CENTER);
        wrapper.add(btnRow,       BorderLayout.SOUTH);

        // Olaylar
        btnSaveEdit.addActionListener(e -> handleSaveEdit());
        btnCancelEdit.addActionListener(e -> cancelEdit());

        return wrapper;
    }

    // ── Aksiyon Butonu Barı ───────────────────────────────────────────────────

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(InventorTheme.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, InventorTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 16, 12, 16)
        ));

        JLabel title = new JLabel("> ROL VE DURUM AKSİYONLARI");
        title.setFont(InventorTheme.pixel(7f));
        title.setForeground(InventorTheme.COL_MUTED);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        btnApprove     = InventorTheme.addButton("ONAYLA / AKTİF ET");
        btnMakeManager = InventorTheme.updateButton("YÖNETİCİ YAP");
        btnMakeStaff   = InventorTheme.clearButton("PERSONEL YAP");
        btnDeactivate  = InventorTheme.retireButton("PASİFE AL");
        btnDelete      = InventorTheme.deleteButton("KULLANICIYI SİL");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(InventorTheme.BG_PANEL);
        btnRow.add(btnApprove);
        btnRow.add(btnMakeManager);
        btnRow.add(btnMakeStaff);
        btnRow.add(btnDeactivate);
        btnRow.add(btnDelete);

        bar.add(title,  BorderLayout.NORTH);
        bar.add(btnRow, BorderLayout.CENTER);

        btnApprove.addActionListener(e     -> updateUserStatus(true, null));
        btnDeactivate.addActionListener(e  -> updateUserStatus(false, null));
        btnMakeManager.addActionListener(e -> updateUserStatus(true, "MANAGER"));
        btnMakeStaff.addActionListener(e   -> updateUserStatus(true, "STAFF"));
        btnDelete.addActionListener(e      -> handleDelete());

        return bar;
    }

    // ── Veri ─────────────────────────────────────────────────────────────────

    private void loadUserData() {
        tableModel.setRowCount(0);
        List<User> users = userController.getAllUsers();
        for (User u : users)
            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getRole().name(),
                    u.isActive() ? "AKTİF" : "ONAY BEKLİYOR"
            });
    }

    // ── Form Doldurma ─────────────────────────────────────────────────────────

    private void fillEditForm() {
        int row = table.getSelectedRow();
        if (row == -1) {
            cancelEdit();
            return;
        }
        int m = table.convertRowIndexToModel(row);
        editingUserId = (int) tableModel.getValueAt(m, 0);
        txtEditUsername.setText(tableModel.getValueAt(m, 1).toString());
        txtEditEmail.setText(tableModel.getValueAt(m, 2).toString());
        btnSaveEdit.setEnabled(true);
        btnCancelEdit.setEnabled(true);
        lblEditTitle.setText("> DÜZENLEME: " + tableModel.getValueAt(m, 1).toString().toUpperCase());
        lblEditTitle.setForeground(InventorTheme.ACCENT);
    }

    private void cancelEdit() {
        editingUserId = -1;
        txtEditUsername.setText("");
        txtEditEmail.setText("");
        btnSaveEdit.setEnabled(false);
        btnCancelEdit.setEnabled(false);
        lblEditTitle.setText("> KULLANICI BİLGİLERİNİ DÜZENLE");
        lblEditTitle.setForeground(InventorTheme.ACCENT2);
        table.clearSelection();
    }

    // ── Handler'lar ───────────────────────────────────────────────────────────

    private void handleSaveEdit() {
        if (editingUserId == -1) return;

        String newUsername = txtEditUsername.getText().trim();
        String newEmail    = txtEditEmail.getText().trim();

        if (newUsername.isEmpty()) {
            showMsg("⚠  Kullanıcı adı boş olamaz.", InventorTheme.COL_YELLOW); return;
        }
        if (newEmail.isEmpty() || !newEmail.contains("@")) {
            showMsg("⚠  Geçerli bir e-posta adresi girin.", InventorTheme.COL_YELLOW); return;
        }

        try {
            if (userController.updateUserInfo(editingUserId, newUsername, newEmail)) {
                showMsg("✔  Kullanıcı bilgileri güncellendi.", InventorTheme.COL_GREEN);
                loadUserData();
                cancelEdit();
            } else {
                showMsg("✕  Güncelleme başarısız. Kullanıcı adı zaten kullanılıyor olabilir.",
                        InventorTheme.COL_RED);
            }
        } catch (Exception ex) {
            showMsg("✕  " + ex.getMessage(), InventorTheme.COL_RED);
        }
    }

    private void updateUserStatus(boolean active, String explicitRole) {
        int row = table.getSelectedRow();
        if (row == -1) { showMsg("⚠  Bir kullanıcı seçin.", InventorTheme.COL_YELLOW); return; }
        int m    = table.convertRowIndexToModel(row);
        int id   = (int) tableModel.getValueAt(m, 0);
        String currentRole = tableModel.getValueAt(m, 3).toString();
        String role = (explicitRole != null) ? explicitRole : currentRole;

        if (userController.updateUserStatus(id, active, role)) {
            showMsg("✔  Kullanıcı güncellendi.", InventorTheme.COL_GREEN);
            loadUserData();
        } else {
            showMsg("✕  Güncelleme başarısız.", InventorTheme.COL_RED);
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { showMsg("⚠  Silinecek kullanıcıyı seçin.", InventorTheme.COL_YELLOW); return; }
        int m  = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(m, 0);
        String u = tableModel.getValueAt(m, 1).toString();
        if (confirmDialog(u + " kullanıcısı silinsin mi?")) {
            try {
                if (userController.deleteUser(id)) {
                    showMsg("✔  Kullanıcı silindi.", InventorTheme.COL_RED);
                    loadUserData();
                    cancelEdit();
                }
            } catch (IllegalStateException ex) {
                showMsg("✕  " + ex.getMessage(), InventorTheme.COL_RED);
            }
        }
    }

    // ── Renderers ────────────────────────────────────────────────────────────

    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            String v = val != null ? val.toString() : "";
            Color c  = v.equals("AKTİF") ? InventorTheme.COL_GREEN : InventorTheme.COL_YELLOW;
            setFont(InventorTheme.vt(15f));
            setForeground(c);
            setBackground(sel ? new Color(0x00FFCC22, true) : InventorTheme.BG_PRIMARY);
            setText("[ " + v + " ]");
            setHorizontalAlignment(CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            return this;
        }
    }

    static class RoleRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            String v = val != null ? val.toString() : "";
            Color c = switch (v.toUpperCase()) {
                case "ADMIN"   -> InventorTheme.COL_RED;
                case "MANAGER" -> InventorTheme.ACCENT2;
                case "STAFF"   -> InventorTheme.COL_BLUE;
                default        -> InventorTheme.TEXT_PRIMARY;
            };
            setFont(InventorTheme.vt(15f));
            setForeground(c);
            setBackground(sel ? new Color(0x00FFCC22, true) : InventorTheme.BG_PRIMARY);
            setText(v);
            setHorizontalAlignment(CENTER);
            return this;
        }
    }
}