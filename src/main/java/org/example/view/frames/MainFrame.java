package org.example.view.frames;

import org.example.model.entity.User;
import org.example.model.enums.Role;
import org.example.util.InventorTheme;
import org.example.util.SessionManager;
import org.example.view.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private JButton btnDashboard, btnProducts, btnCategories, btnInventory,
            btnSuppliers, btnWarehouses, btnWarehouseStock, btnAssets, btnUsers, btnLogout;
    private JPanel sideMenu, contentArea;
    private JButton activeButton = null;
    private JPanel adminDivider;  // YÖNETİM başlığı — rol bazlı gizlenir

    public MainFrame() {
        User currentUser = SessionManager.getCurrentUser();
        setTitle("inventory.io — " + currentUser.getUsername().toUpperCase());
        setMinimumSize(new Dimension(1000, 650));
        setSize(1280, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        InventorTheme.styleFrame(this);

        initComponents();
        initEventHandlers();
        applyRolePermissions();
        setActive(btnDashboard);
        switchPanel(createDashboard());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ANA LAYOUT
    // ═════════════════════════════════════════════════════════════════════════

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(InventorTheme.BG_PRIMARY);

        sideMenu = buildSidebar();
        add(sideMenu, BorderLayout.WEST);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(InventorTheme.BG_PRIMARY);
        add(contentArea, BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(InventorTheme.BG_SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, InventorTheme.BORDER));

        // ── 1. LOGO BLOĞU ─────────────────────────────────────────────────
        panel.add(buildLogoBlock());

        // ── 2. KULLANICI BLOĞU ────────────────────────────────────────────
        panel.add(buildUserBlock());

        // ── 3. NAV TABS (widget'taki px-nav-tabs stili) ───────────────────
        panel.add(buildNavSection("ANA MENÜ"));

        btnDashboard  = navButton("Dashboard",          "◈");
        btnProducts   = navButton("Ürün Yönetimi",    "◉");
        btnCategories = navButton("Kategori Yönetimi","◈");
        btnInventory  = navButton("Stok Hareketleri", "◉");
        btnSuppliers  = navButton("Tedarikçiler",     "◈");
        btnWarehouses     = navButton("Depolar",           "◉");
        btnWarehouseStock = navButton("Depo Stok Durumu", "◈");
        btnAssets         = navButton("Demirbaşlar",       "◉");

        panel.add(btnDashboard);
        panel.add(btnProducts);
        panel.add(btnCategories);
        panel.add(btnInventory);
        panel.add(btnSuppliers);
        panel.add(btnWarehouses);
        panel.add(btnWarehouseStock);
        panel.add(btnAssets);

        adminDivider = buildNavSection("YÖNETİM");
        panel.add(adminDivider);
        btnUsers = navButton("Kullanıcı Yönetimi", "◉");
        panel.add(btnUsers);

        panel.add(Box.createVerticalGlue());
        btnLogout = buildLogoutButton();
        panel.add(btnLogout);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOGO BLOĞU — widget'taki px-logo stili, köşe dekorasyonu, blink cursor
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildLogoBlock() {
        // Köşe dekorasyonu paintComponent ile
        JPanel block = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF); // pixel-perfect
                // Köşe L şekilleri
                g2.setColor(InventorTheme.ACCENT);
                int s = 10, t = 2;
                // Sol üst
                g2.fillRect(0,          0,          s, t);
                g2.fillRect(0,          0,          t, s);
                // Sağ üst
                g2.fillRect(getWidth()-s, 0,         s, t);
                g2.fillRect(getWidth()-t, 0,         t, s);
                // Sol alt
                g2.fillRect(0,          getHeight()-t, s, t);
                g2.fillRect(0,          getHeight()-s, t, s);
                // Sağ alt
                g2.fillRect(getWidth()-s, getHeight()-t, s, t);
                g2.fillRect(getWidth()-t, getHeight()-s, t, s);
            }
        };
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBackground(InventorTheme.BG_PANEL);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        block.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, InventorTheme.ACCENT),
                BorderFactory.createEmptyBorder(22, 20, 18, 20)
        ));

        // INVENTOR.IO büyük yazı
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoRow.setBackground(InventorTheme.BG_PANEL);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblInv = new JLabel("INVENTORY");
        lblInv.setFont(InventorTheme.pixel(16f));
        lblInv.setForeground(InventorTheme.ACCENT);

        JLabel lblDot = new JLabel(".IO");
        lblDot.setFont(InventorTheme.pixel(16f));
        lblDot.setForeground(InventorTheme.ACCENT2);

        logoRow.add(lblInv);
        logoRow.add(lblDot);

        // Alt açıklama
        JLabel subLbl = new JLabel("ENVANTER YÖNETİM SİSTEMİ");
        subLbl.setFont(InventorTheme.pixel(5f));
        subLbl.setForeground(InventorTheme.COL_MUTED);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        subLbl.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        block.add(logoRow);
        block.add(subLbl);
        return block;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  KULLANICI BLOĞU — SİSTEM AKTİF dot, kullanıcı adı + rol
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildUserBlock() {
        JPanel block = new JPanel(new BorderLayout(10, 0));
        block.setBackground(InventorTheme.BG_PANEL);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        block.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, InventorTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Sol: pixel yeşil dot (SİSTEM AKTİF göstergesi)
        JPanel dotPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(InventorTheme.ACCENT);
                int s = 8;
                int y = (getHeight() - s) / 2;
                g.fillRect(0, y, s, s); // pixel square dot
            }
        };
        dotPanel.setBackground(InventorTheme.BG_PANEL);
        dotPanel.setPreferredSize(new Dimension(14, 20));

        // Sağ: kullanıcı bilgileri
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(InventorTheme.BG_PANEL);

        String uname = SessionManager.getCurrentUser().getUsername().toUpperCase();
        String role  = SessionManager.getCurrentUser().getRole().name();

        JLabel lblName = new JLabel(uname);
        lblName.setFont(InventorTheme.pixel(10f));
        lblName.setForeground(InventorTheme.TEXT_PRIMARY);

        JLabel lblRole = new JLabel(role);
        lblRole.setFont(InventorTheme.pixel(8f));
        lblRole.setForeground(InventorTheme.ACCENT2);

        info.add(lblName);
        info.add(Box.createRigidArea(new Dimension(0, 4)));
        info.add(lblRole);

        // Sağ: versiyon
        JLabel lblVer = new JLabel("v2.4");
        lblVer.setFont(InventorTheme.pixel(6f));
        lblVer.setForeground(InventorTheme.COL_MUTED);

        block.add(dotPanel, BorderLayout.WEST);
        block.add(info,     BorderLayout.CENTER);
        block.add(lblVer,   BorderLayout.EAST);
        return block;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  NAV BÖLÜM BAŞLIĞI — widget'taki px-section-title stili
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildNavSection(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(InventorTheme.BG_SURFACE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createEmptyBorder(10, 16, 4, 0));

        JLabel lbl = new JLabel(title);
        lbl.setFont(InventorTheme.pixel(9f));
        lbl.setForeground(InventorTheme.ACCENT);
        p.add(lbl, BorderLayout.WEST);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  NAV BUTONU — widget'taki px-tab stili: sol border aktifken
    // ─────────────────────────────────────────────────────────────────────────

    private JButton navButton(String text, String icon) {
        // FIX: paintComponent+drawString yerine standart JButton kullanılıyor.
        // drawString pixel font ile Türkçe karakterleri bozuyordu (ğ, ş, ı vb.)
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());

        // Sol accent çizgisi için özel panel (aktifken görünür)
        JPanel accentBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (btn.getForeground().equals(InventorTheme.ACCENT)) {
                    g.setColor(InventorTheme.ACCENT);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        accentBar.setPreferredSize(new Dimension(3, 0));
        accentBar.setBackground(InventorTheme.BG_SURFACE);
        accentBar.setOpaque(false);

        // İkon + metin label — Swing'in kendi text render'ı Türkçe'yi doğru çizer
        JLabel lbl = new JLabel(icon + "  " + text);
        lbl.setFont(InventorTheme.pixel(12f));
        lbl.setForeground(InventorTheme.COL_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        btn.add(accentBar, BorderLayout.WEST);
        btn.add(lbl,       BorderLayout.CENTER);

        btn.setFont(InventorTheme.pixel(12f));
        btn.setForeground(InventorTheme.COL_MUTED);
        btn.setBackground(InventorTheme.BG_SURFACE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setPreferredSize(new Dimension(230, 52));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (activeButton != btn) {
                    lbl.setForeground(InventorTheme.TEXT_PRIMARY);
                    btn.setOpaque(true);
                    btn.setBackground(InventorTheme.BG_PANEL);
                    accentBar.setBackground(InventorTheme.BG_PANEL);
                    btn.repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (activeButton != btn) {
                    lbl.setForeground(InventorTheme.COL_MUTED);
                    btn.setBackground(InventorTheme.BG_SURFACE);
                    accentBar.setBackground(InventorTheme.BG_SURFACE);
                    btn.repaint();
                }
            }
        });
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AKTİF BUTON — sol neon çizgi + accent renk
    // ─────────────────────────────────────────────────────────────────────────

    private void setActive(JButton btn) {
        if (activeButton != null) {
            activeButton.setForeground(InventorTheme.COL_MUTED);
            activeButton.setBackground(InventorTheme.BG_SURFACE);
            // İç label rengini de sıfırla
            setButtonLabelColor(activeButton, InventorTheme.COL_MUTED);
            activeButton.repaint();
        }
        activeButton = btn;
        btn.setForeground(InventorTheme.ACCENT);
        btn.setBackground(new Color(0x00FFCC1A, true));
        setButtonLabelColor(btn, InventorTheme.ACCENT);
        btn.repaint();
    }

    /** BorderLayout içindeki JLabel'ın rengini güncelle */
    private void setButtonLabelColor(JButton btn, Color color) {
        for (Component c : btn.getComponents()) {
            if (c instanceof JLabel lbl) {
                lbl.setForeground(color);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ÇIKIŞ BUTONU
    // ─────────────────────────────────────────────────────────────────────────

    private JButton buildLogoutButton() {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());

        // Üst ayraç çizgisi
        JPanel topLine = new JPanel();
        topLine.setBackground(InventorTheme.BORDER);
        topLine.setPreferredSize(new Dimension(0, 2));

        JLabel lbl = new JLabel("◀  ÇIKIŞ YAP");
        lbl.setFont(InventorTheme.pixel(12f));
        lbl.setForeground(InventorTheme.COL_RED);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        btn.add(topLine, BorderLayout.NORTH);
        btn.add(lbl,     BorderLayout.CENTER);

        btn.setFont(InventorTheme.pixel(12f));
        btn.setForeground(InventorTheme.COL_RED);
        btn.setBackground(InventorTheme.BG_SURFACE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setPreferredSize(new Dimension(250, 52));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(50, 8, 18));
                btn.setOpaque(true);
                btn.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(InventorTheme.BG_SURFACE);
                btn.repaint();
            }
        });
        return btn;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PANEL DEĞİŞTİRME
    // ═════════════════════════════════════════════════════════════════════════

    private DashboardPanel createDashboard() {
        return new DashboardPanel(
            () -> { setActive(btnProducts); switchPanel(new ProductManagementPanel()); },
            () -> { setActive(btnProducts); switchPanel(new ProductManagementPanel(true)); },
            () -> { setActive(btnUsers);    switchPanel(new UserManagementPanel()); }
        );
    }

    private void switchPanel(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  EVENT HANDLERS
    // ═════════════════════════════════════════════════════════════════════════

    private void initEventHandlers() {
        btnDashboard.addActionListener(e  -> { setActive(btnDashboard);  switchPanel(createDashboard()); });
        btnProducts.addActionListener(e   -> { setActive(btnProducts);   switchPanel(new ProductManagementPanel()); });
        btnCategories.addActionListener(e -> { setActive(btnCategories); switchPanel(new CategoryManagementPanel()); });
        btnInventory.addActionListener(e  -> { setActive(btnInventory);  switchPanel(new InventoryTransactionPanel()); });
        btnSuppliers.addActionListener(e  -> { setActive(btnSuppliers);  switchPanel(new SupplierManagementPanel()); });
        btnWarehouses.addActionListener(e     -> { setActive(btnWarehouses);     switchPanel(new WarehouseManagementPanel()); });
        btnWarehouseStock.addActionListener(e -> { setActive(btnWarehouseStock); switchPanel(new WarehouseStockPanel()); });
        btnAssets.addActionListener(e         -> { setActive(btnAssets);         switchPanel(new AssetManagementPanel()); });
        btnUsers.addActionListener(e      -> { setActive(btnUsers);      switchPanel(new UserManagementPanel()); });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Oturumu kapatmak istediğinize emin misiniz?",
                    "⚠  ÇIKIŞ", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SessionManager.clearSession();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ROL YETKİLERİ
    // ═════════════════════════════════════════════════════════════════════════

    private void applyRolePermissions() {
        Role role = SessionManager.getCurrentUser().getRole();
        if (role == Role.STAFF) {
            btnUsers.setVisible(false);
            btnSuppliers.setVisible(false);
            btnWarehouses.setVisible(false);
            btnCategories.setVisible(false);
            btnAssets.setVisible(false);
            // YÖNETİM başlığını ve altındaki tüm elemanları gizle
            if (adminDivider != null) adminDivider.setVisible(false);
        } else if (role == Role.MANAGER) {
            btnUsers.setVisible(false);
            if (adminDivider != null) adminDivider.setVisible(false);
        }
    }
}