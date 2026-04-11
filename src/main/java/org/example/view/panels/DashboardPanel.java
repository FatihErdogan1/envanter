package org.example.view.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.example.controller.DashboardController;
import org.example.model.enums.Role;
import org.example.util.InventorTheme;
import org.example.util.SessionManager;

public class DashboardPanel extends JPanel {

    private final DashboardController dashboardController;

    // Navigasyon callback'leri — null ise kart tıklanamaz
    private final Runnable onNavigateToProducts;
    private final Runnable onNavigateToWarehouseStock;
    private final Runnable onNavigateToUsers;

    /** Callback'siz varsayılan constructor — kartlar tıklanamaz olur. */
    public DashboardPanel() {
        this(null, null, null);
    }

    public DashboardPanel(Runnable onNavigateToProducts,
                          Runnable onNavigateToWarehouseStock,
                          Runnable onNavigateToUsers) {
        this.dashboardController        = new DashboardController();
        this.onNavigateToProducts       = onNavigateToProducts;
        this.onNavigateToWarehouseStock = onNavigateToWarehouseStock;
        this.onNavigateToUsers          = onNavigateToUsers;
        setLayout(new BorderLayout(0, 0));
        setBackground(InventorTheme.BG_PRIMARY);
        initComponents();
    }

    private void initComponents() {

        // ── ÜST BAŞLIK BARRI ────────────────────────────────────────────────
        JPanel headerBar = buildHeaderBar();
        add(headerBar, BorderLayout.NORTH);

        // ── ORTA: Stat Kartları + Alt Bilgi ─────────────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setBackground(InventorTheme.BG_PRIMARY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Stat kartları
        int totalProducts = dashboardController.getTotalProductCount();
        int lowStock      = dashboardController.getLowStockCount();

        boolean isAdmin = SessionManager.getCurrentUser().getRole() == Role.ADMIN;
        int columns = isAdmin ? 3 : 2;

        JPanel cardRow = new JPanel(new GridLayout(1, columns, 16, 0));
        cardRow.setBackground(InventorTheme.BG_PRIMARY);
        cardRow.add(buildStatCard("TOPLAM ÜRÜN", String.valueOf(totalProducts), InventorTheme.ACCENT,  "◈", onNavigateToProducts));
        cardRow.add(buildStatCard("KRİTİK STOK", String.valueOf(lowStock),      InventorTheme.COL_RED, "⚠", onNavigateToWarehouseStock));
        if (isAdmin) {
            int totalUsers = dashboardController.getTotalUserCount();
            cardRow.add(buildStatCard("SİSTEM KULLANICI", String.valueOf(totalUsers), InventorTheme.COL_GREEN, "◉", onNavigateToUsers));
        }

        // Alt bilgi şeridi
        JPanel footerStrip = buildFooterStrip();

        centerPanel.add(cardRow,     BorderLayout.NORTH);
        centerPanel.add(buildActivityPlaceholder(), BorderLayout.CENTER);
        centerPanel.add(footerStrip, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    // ── Üst Başlık ────────────────────────────────────────────────────────────

    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(InventorTheme.BG_SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, InventorTheme.BORDER),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));

        JLabel title = new JLabel("▶  SİSTEM ÖZETİ");
        title.setFont(InventorTheme.pixel(10f));
        title.setForeground(InventorTheme.ACCENT);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        JLabel dateLabel = new JLabel(today);
        dateLabel.setFont(InventorTheme.pixel(8f));
        dateLabel.setForeground(InventorTheme.COL_MUTED);

        bar.add(title,     BorderLayout.WEST);
        bar.add(dateLabel, BorderLayout.EAST);
        return bar;
    }

    // ── Stat Kartı ────────────────────────────────────────────────────────────

    private JPanel buildStatCard(String title, String value, Color accent, String icon, Runnable onClick) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Pixel köşe dekorasyonu
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(accent);
                int s = 6;
                // Sol üst
                g2.fillRect(0, 0, s, 2);
                g2.fillRect(0, 0, 2, s);
                // Sağ üst
                g2.fillRect(getWidth()-s, 0, s, 2);
                g2.fillRect(getWidth()-2, 0, 2, s);
                // Sol alt
                g2.fillRect(0, getHeight()-2, s, 2);
                g2.fillRect(0, getHeight()-s, 2, s);
                // Sağ alt
                g2.fillRect(getWidth()-s, getHeight()-2, s, 2);
                g2.fillRect(getWidth()-2, getHeight()-s, 2, s);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(InventorTheme.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(InventorTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(28, 20, 28, 20)
        ));

        // İkon
        JLabel lblIcon = new JLabel(icon + "  " + title);
        lblIcon.setFont(InventorTheme.pixel(7f));
        lblIcon.setForeground(accent);
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Değer
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(InventorTheme.pixel(36f));
        lblValue.setForeground(accent);
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Alt çizgi
        JPanel line = new JPanel();
        line.setBackground(accent);
        line.setMaximumSize(new Dimension(40, 2));
        line.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblIcon);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(lblValue);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(line);

        if (onClick != null) {
            Border normalBorder = card.getBorder();
            Border hoverBorder  = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accent, 2),
                    BorderFactory.createEmptyBorder(27, 19, 27, 19));
            Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

            MouseAdapter ma = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    card.setBorder(hoverBorder);
                    card.setCursor(hand);
                    card.repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    card.setBorder(normalBorder);
                    card.setCursor(Cursor.getDefaultCursor());
                    card.repaint();
                }
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            };
            addMouseListenerRecursively(card, ma);
        }

        return card;
    }

    /** MouseListener'ı verilen bileşene ve tüm alt bileşenlerine ekler. */
    private void addMouseListenerRecursively(Component comp, MouseAdapter ma) {
        comp.addMouseListener(ma);
        if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                addMouseListenerRecursively(child, ma);
            }
        }
    }

    // ── Aktivite Alanı (placeholder) ─────────────────────────────────────────

    private JPanel buildActivityPlaceholder() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(InventorTheme.BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(InventorTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("▶  SİSTEM AKTİVİTESİ");
        title.setFont(InventorTheme.pixel(7f));
        title.setForeground(InventorTheme.ACCENT2);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JTextArea log = new JTextArea();
        log.setBackground(InventorTheme.BG_PRIMARY);
        log.setForeground(InventorTheme.COL_MUTED);
        log.setFont(InventorTheme.vt(15f));
        log.setEditable(false);
        log.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        log.setText(
                "> Sistem başlatıldı...\n" +
                        "> Veritabanı bağlantısı kuruldu.\n" +
                        "> Oturum açıldı: " + java.time.LocalTime.now().format(
                        DateTimeFormatter.ofPattern("HH:mm:ss")) + "\n" +
                        "> Hazır. _"
        );

        JScrollPane scroll = new JScrollPane(log);
        scroll.setBackground(InventorTheme.BG_PRIMARY);
        scroll.getViewport().setBackground(InventorTheme.BG_PRIMARY);
        scroll.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 1));

        p.add(title,  BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Alt Footer ────────────────────────────────────────────────────────────

    private JPanel buildFooterStrip() {
        JPanel strip = new JPanel(new BorderLayout());
        strip.setBackground(InventorTheme.BG_SURFACE);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, InventorTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));

        JLabel lbl = new JLabel("inventor.io  |  v1.0  |  " + LocalDate.now());
        lbl.setFont(InventorTheme.pixel(6f));
        lbl.setForeground(InventorTheme.COL_MUTED);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        strip.add(lbl, BorderLayout.CENTER);
        return strip;
    }
}