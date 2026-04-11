package org.example.view.frames;

import org.example.controller.UserController;
import org.example.model.entity.User;
import org.example.util.InventorTheme;
import org.example.util.SessionManager;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final UserController userController;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister;

    public LoginFrame() {
        this.userController = new UserController();

        setTitle("inventory.io — Giriş");
        setSize(480, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        // ── KART ───────────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(InventorTheme.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(InventorTheme.ACCENT, 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        // ── LOGO ──────────────────────────────────────────────────────────
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoRow.setOpaque(false);
        JLabel lblLogo = new JLabel("INVENTORY");
        lblLogo.setFont(InventorTheme.pixel(22f));
        lblLogo.setForeground(InventorTheme.ACCENT);
        JLabel lblDot = new JLabel(".IO");
        lblDot.setFont(InventorTheme.pixel(22f));
        lblDot.setForeground(InventorTheme.ACCENT2);
        logoRow.add(lblLogo);
        logoRow.add(lblDot);

        JLabel lblSub = new JLabel("DEMİRBAŞ YÖNETİM SİSTEMİ");
        lblSub.setFont(InventorTheme.pixel(7f));
        lblSub.setForeground(InventorTheme.COL_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── INPUTLAR ──────────────────────────────────────────────────────
        txtUsername = createStyledField(false);
        txtPassword = (JPasswordField) createStyledField(true);

        // ── BUTONLAR ──────────────────────────────────────────────────────
        btnLogin = new JButton("[ GİRİŞ YAP ]");
        styleMainButton(btnLogin, InventorTheme.ACCENT);

        btnRegister = new JButton("[ HESAP OLUŞTUR ]");
        styleSecondaryButton(btnRegister);

        // ── YERLEŞTİRME ───────────────────────────────────────────────────
        card.add(logoRow);
        card.add(Box.createVerticalStrut(10));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(40));

        card.add(createLabel("> KULLANICI ADI"));
        card.add(Box.createVerticalStrut(8));
        card.add(txtUsername);

        card.add(Box.createVerticalStrut(20));

        card.add(createLabel("> ŞİFRE"));
        card.add(Box.createVerticalStrut(8));
        card.add(txtPassword);

        card.add(Box.createVerticalStrut(40));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(15));
        card.add(btnRegister);

        root.add(card);

        // EVENTLER
        initEvents();
    }

    private void initEvents() {
        // GİRİŞ BUTONU TIKLAMA
        btnLogin.addActionListener(e -> handleLogin());

        // KAYIT BUTONU TIKLAMA (Burası RegisterFrame'e gönderir)
        btnRegister.addActionListener(e -> {
            try {
                // Eğer RegisterFrame sınıfın farklı bir isimdeyse burayı düzelt
                new RegisterFrame().setVisible(true);
                this.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Kayıt ekranı açılamadı!");
                ex.printStackTrace();
            }
        });

        // Şifrede Enter'a basınca giriş yap
        txtPassword.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String userStr = txtUsername.getText().trim();
        String passStr = new String(txtPassword.getPassword());

        if(userStr.isEmpty() || passStr.isEmpty()){
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun.");
            return;
        }

        try {
            User user = userController.login(userStr, passStr);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                // ANA EKRANI AÇ (Sınıf adının MainFrame olduğundan emin ol)
                new MainFrame().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Hatalı kullanıcı adı veya şifre!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    // ── YARDIMCI GÖRSEL METOTLAR (Hata payını azaltmak için içeride) ───────

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(InventorTheme.pixel(7f));
        l.setForeground(InventorTheme.COL_MUTED);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JTextField createStyledField(boolean isPass) {
        JTextField f = isPass ? new JPasswordField() : new JTextField();
        f.setFont(InventorTheme.vt(18f));
        f.setBackground(InventorTheme.BG_PRIMARY);
        f.setForeground(InventorTheme.TEXT_PRIMARY);
        f.setCaretColor(InventorTheme.ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(InventorTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        f.setMaximumSize(new Dimension(320, 40));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        if(isPass) ((JPasswordField)f).setEchoChar('●');
        return f;
    }

    private void styleMainButton(JButton btn, Color color) {
        btn.setFont(InventorTheme.pixel(10f));
        btn.setBackground(color);
        btn.setForeground(InventorTheme.BG_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setMaximumSize(new Dimension(320, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(InventorTheme.pixel(8f));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(InventorTheme.COL_MUTED);
        btn.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 1));
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(320, 35));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}