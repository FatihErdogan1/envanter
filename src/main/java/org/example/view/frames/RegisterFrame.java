package org.example.view.frames;

import com.formdev.flatlaf.FlatClientProperties;
import org.example.controller.UserController;
import org.example.util.InventorTheme;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegisterFrame extends JFrame {

    private final UserController userController;
    private JTextField txtUsername, txtEmail;
    private JPasswordField txtPassword;
    private JButton btnRegister, btnBack;

    public RegisterFrame() {
        this.userController = new UserController();

        setTitle("inventor.io — Yeni Kayıt");
        setSize(480, 640); // Boyutu biraz artırdık, alanlar nefes alsın
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Temayı pencere geneline uygula
        InventorTheme.styleFrame(this);

        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(InventorTheme.BG_PRIMARY);
        setContentPane(root);

        // ── Kart ─────────────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(InventorTheme.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(InventorTheme.ACCENT2, 1), // Turuncu vurgulu ince border
                BorderFactory.createEmptyBorder(25, 45, 25, 45)
        ));

        // ── Logo ──────────────────────────────────────────────────────────────
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoMain = new JLabel("INVENTOR");
        logoMain.setFont(InventorTheme.pixel(20f));
        logoMain.setForeground(InventorTheme.ACCENT);

        JLabel logoDot = new JLabel(".IO");
        logoDot.setFont(InventorTheme.pixel(20f));
        logoDot.setForeground(InventorTheme.ACCENT2);

        logoRow.add(logoMain);
        logoRow.add(logoDot);

        JLabel lblSub = new JLabel("YENİ HESAP OLUŞTUR");
        lblSub.setFont(InventorTheme.pixel(7f));
        lblSub.setForeground(InventorTheme.COL_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Pixel divider
        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(InventorTheme.BORDER);
                g.fillRect(0, 5, getWidth(), 1);
                g.setColor(InventorTheme.ACCENT2);
                g.fillRect(getWidth()/2 - 30, 4, 60, 3);
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(320, 12));

        // ── Prompt ────────────────────────────────────────────────────────────
        JLabel lblPrompt = new JLabel("> KAYIT FORMU");
        lblPrompt.setFont(InventorTheme.pixel(8f));
        lblPrompt.setForeground(InventorTheme.ACCENT2);
        lblPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Alanlar ───────────────────────────────────────────────────────────
        txtUsername = createStyledField(false, "kullanici_adi");
        txtEmail    = createStyledField(false, "ornek@mail.com");
        txtPassword = (JPasswordField) createStyledField(true, "••••••••");

        // ── Bilgi notu ────────────────────────────────────────────────────────
        JLabel lblInfo = new JLabel("⚠  Kayıt sonrası yönetici onayı gereklidir.");
        lblInfo.setFont(InventorTheme.pixel(6f));
        lblInfo.setForeground(InventorTheme.COL_YELLOW);
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Butonlar ──────────────────────────────────────────────────────────
        btnRegister = buildRegisterButton();
        btnBack     = buildBackButton();

        // ── Karta Ekle ────────────────────────────────────────────────────────
        card.add(logoRow);
        card.add(Box.createVerticalStrut(8));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(20));
        card.add(divider);
        card.add(Box.createVerticalStrut(20));
        card.add(lblPrompt);
        card.add(Box.createVerticalStrut(20));

        card.add(fieldLabel("KULLANICI ADI"));
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsername);

        card.add(Box.createVerticalStrut(15));

        card.add(fieldLabel("E-POSTA"));
        card.add(Box.createVerticalStrut(6));
        card.add(txtEmail);

        card.add(Box.createVerticalStrut(15));

        card.add(fieldLabel("ŞİFRE"));
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);

        card.add(Box.createVerticalStrut(15));
        card.add(lblInfo);

        card.add(Box.createVerticalStrut(30));
        card.add(btnRegister);
        card.add(Box.createVerticalStrut(12));
        card.add(btnBack);

        root.add(card);
        getRootPane().setDefaultButton(btnRegister);
        initEventHandlers();
    }

    // ── Yardımcı UI Metodları ──────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(InventorTheme.pixel(7f));
        lbl.setForeground(InventorTheme.COL_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JTextField createStyledField(boolean isPass, String placeholder) {
        JTextField f = isPass ? new JPasswordField() : new JTextField();
        f.setFont(InventorTheme.vt(18f));
        f.setBackground(InventorTheme.BG_PRIMARY);
        f.setForeground(InventorTheme.TEXT_PRIMARY);
        f.setCaretColor(InventorTheme.ACCENT2); // Register sayfasında turuncu vurgu
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);

        Border normalBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(InventorTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));

        Border activeBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(InventorTheme.ACCENT2, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));

        f.setBorder(normalBorder);
        f.setMaximumSize(new Dimension(320, 42));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        if(isPass) ((JPasswordField)f).setEchoChar('●');

        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.setBorder(activeBorder); }
            @Override public void focusLost(FocusEvent e) { f.setBorder(normalBorder); }
        });
        return f;
    }

    private JButton buildRegisterButton() {
        JButton btn = new JButton("[ KAYDI TAMAMLA ]");
        btn.setFont(InventorTheme.pixel(10f));
        btn.setForeground(InventorTheme.BG_PRIMARY);
        btn.setBackground(InventorTheme.ACCENT2);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(320, 48));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(0xFF8C55)); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(InventorTheme.ACCENT2); }
        });
        return btn;
    }

    private JButton buildBackButton() {
        JButton btn = new JButton("[ GİRİŞ SAYFASINA DÖN ]");
        btn.setFont(InventorTheme.pixel(8f));
        btn.setForeground(InventorTheme.COL_MUTED);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(320, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setForeground(InventorTheme.TEXT_PRIMARY);
                btn.setBorder(BorderFactory.createLineBorder(InventorTheme.COL_MUTED, 1));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setForeground(InventorTheme.COL_MUTED);
                btn.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 1));
            }
        });
        return btn;
    }

    private void initEventHandlers() {
        btnRegister.addActionListener(e -> handleRegister());
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if(username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!", "EKSİK BİLGİ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean ok = userController.registerUser(username, password, email, "STAFF");
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "✔ Kayıt başvurunuz alındı!\nYönetici onayı sonrası giriş yapabilirsiniz.",
                        "BAŞARILI", JOptionPane.INFORMATION_MESSAGE);
                new LoginFrame().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Kayıt yapılamadı.", "HATA", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "SİSTEM HATASI", JOptionPane.ERROR_MESSAGE);
        }
    }
}