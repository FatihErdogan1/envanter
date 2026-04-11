package org.example.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.InputStream;

public class InventorTheme {

    public static final Color BG_PRIMARY   = new Color(0x0D0D1A);
    public static final Color BG_SURFACE   = new Color(0x13132B);
    public static final Color BG_PANEL     = new Color(0x1A1A35);
    public static final Color BORDER       = new Color(0x2A2A5A);
    public static final Color ACCENT       = new Color(0x00FFCC);
    public static final Color ACCENT2      = new Color(0xFF6B35);
    public static final Color COL_YELLOW   = new Color(0xFFE566);
    public static final Color COL_BLUE     = new Color(0x4488FF);
    public static final Color COL_RED      = new Color(0xFF4466);
    public static final Color COL_GREEN    = new Color(0x44FF88);
    public static final Color COL_MUTED    = new Color(0x6666AA);
    public static final Color TEXT_PRIMARY = new Color(0xCCCCFF);
    public static final Color TEXT_WHITE   = new Color(0xEEEEFF);

    private static Font pixelFont;
    private static Font vt323Font;

    public static void apply() {
        loadFonts();
        setupFlatLaf();
        applyUIDefaults();
    }

    private static void loadFonts() {
        pixelFont = loadFont("/fonts/PressStart2P-Regular.ttf", 9f);
        vt323Font = loadFont("/fonts/VT323-Regular.ttf", 16f);
        if (pixelFont == null) pixelFont = new Font(Font.MONOSPACED, Font.PLAIN, 9);
        if (vt323Font == null) vt323Font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    }

    private static Font loadFont(String path, float size) {
        try (InputStream is = InventorTheme.class.getResourceAsStream(path)) {
            if (is == null) return null;
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        } catch (Exception e) { return null; }
    }

    public static Font pixel(float size) { return pixelFont.deriveFont(size); }
    public static Font vt(float size)    { return vt323Font.deriveFont(size); }

    private static void setupFlatLaf() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Panel.background",             BG_PRIMARY);
            UIManager.put("RootPane.background",          BG_PRIMARY);
            UIManager.put("Frame.background",             BG_PRIMARY);
            UIManager.put("Component.background",         BG_SURFACE);
            UIManager.put("Component.focusColor",         ACCENT);
            UIManager.put("Component.focusedBorderColor", ACCENT);
            UIManager.put("Component.borderColor",        BORDER);
            UIManager.put("ScrollBar.thumb",              BG_PANEL);
            UIManager.put("ScrollBar.thumbHover",         COL_MUTED);
            UIManager.put("ScrollBar.track",              BG_SURFACE);
            UIManager.put("ScrollBar.width",              8);
        } catch (Exception e) {
            System.err.println("[InventorTheme] " + e.getMessage());
        }
    }

    private static void applyUIDefaults() {
        Font bodyFont  = vt(15f);
        Font labelFont = pixel(8f);

        UIManager.put("defaultFont",                      new FontUIResource(bodyFont));
        UIManager.put("Label.font",                       new FontUIResource(labelFont));
        UIManager.put("Label.foreground",                 new ColorUIResource(COL_MUTED));

        UIManager.put("TextField.font",                   new FontUIResource(bodyFont));
        UIManager.put("TextField.foreground",             new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("TextField.background",             new ColorUIResource(BG_PRIMARY));
        UIManager.put("TextField.caretForeground",        new ColorUIResource(ACCENT));
        UIManager.put("TextField.selectionBackground",    new ColorUIResource(new Color(0x00FFCC33, true)));
        UIManager.put("TextField.selectionForeground",    new ColorUIResource(TEXT_WHITE));
        UIManager.put("TextField.border",                 pixelBorder(BORDER));

        UIManager.put("ComboBox.font",                    new FontUIResource(bodyFont));
        UIManager.put("ComboBox.foreground",              new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("ComboBox.background",              new ColorUIResource(BG_PRIMARY));
        UIManager.put("ComboBox.buttonBackground",        new ColorUIResource(BG_PANEL));
        UIManager.put("ComboBox.border",                  pixelBorder(BORDER));

        // FIX: pixel(9f) + compound border with padding — okunabilir buton boyutu
        UIManager.put("Button.font",                      new FontUIResource(pixel(9f)));
        UIManager.put("Button.foreground",                new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("Button.background",                new ColorUIResource(BG_PANEL));
        UIManager.put("Button.hoverBackground",           new ColorUIResource(BG_PANEL));
        UIManager.put("Button.pressedBackground",         new ColorUIResource(BORDER));
        UIManager.put("Button.arc",                       0);

        UIManager.put("Table.font",                       new FontUIResource(bodyFont));
        UIManager.put("Table.foreground",                 new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("Table.background",                 new ColorUIResource(BG_PRIMARY));
        UIManager.put("Table.alternateRowColor",          new ColorUIResource(BG_SURFACE));
        UIManager.put("Table.selectionBackground",        new ColorUIResource(new Color(0x00FFCC22, true)));
        UIManager.put("Table.selectionForeground",        new ColorUIResource(ACCENT));
        UIManager.put("Table.gridColor",                  new ColorUIResource(BORDER));
        UIManager.put("Table.showHorizontalLines",        true);
        UIManager.put("Table.showVerticalLines",          true);

        UIManager.put("TableHeader.font",                 new FontUIResource(pixel(7f)));
        UIManager.put("TableHeader.foreground",           new ColorUIResource(ACCENT));
        UIManager.put("TableHeader.background",           new ColorUIResource(BG_SURFACE));
        UIManager.put("TableHeader.cellBorder",           new LineBorder(BORDER, 1));

        UIManager.put("ScrollPane.background",            new ColorUIResource(BG_PRIMARY));
        UIManager.put("ScrollPane.border",                pixelBorder(BORDER));

        UIManager.put("TitledBorder.titleFont",           new FontUIResource(pixel(8f)));
        UIManager.put("TitledBorder.titleColor",          new ColorUIResource(ACCENT2));
        UIManager.put("TitledBorder.border",              pixelBorder(BORDER));

        UIManager.put("OptionPane.background",            new ColorUIResource(BG_PANEL));
        UIManager.put("OptionPane.messageForeground",     new ColorUIResource(TEXT_PRIMARY));
        UIManager.put("OptionPane.messageFont",           new FontUIResource(bodyFont));
        UIManager.put("OptionPane.buttonFont",            new FontUIResource(pixel(8f)));

        UIManager.put("ToolTip.background",               new ColorUIResource(BG_PANEL));
        UIManager.put("ToolTip.foreground",               new ColorUIResource(ACCENT));
        UIManager.put("ToolTip.border",                   pixelBorder(ACCENT));
        UIManager.put("ToolTip.font",                     new FontUIResource(pixel(7f)));
    }

    private static Border pixelBorder(Color color) {
        return BorderFactory.createLineBorder(color, 2);
    }

    // ── Buton Fabrikaları — FIX: 9f font, uygun padding, opaque=true ─────────

    public static JButton addButton(String text)    { return styledButton(text, COL_GREEN); }
    public static JButton updateButton(String text) { return styledButton(text, COL_BLUE);  }
    public static JButton deleteButton(String text) { return styledButton(text, COL_RED);   }
    public static JButton clearButton(String text)  { return styledButton(text, COL_MUTED); }
    public static JButton retireButton(String text) { return styledButton(text, ACCENT2);   }
    public static JButton searchButton(String text) { return styledButton(text, ACCENT);    }

    private static JButton styledButton(String text, Color accent) {
        JButton btn = new JButton("[ " + text + " ]");
        btn.setFont(pixel(9f));
        btn.setForeground(accent);
        btn.setBackground(BG_PRIMARY);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        Color hoverBg = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25);
        Color pressBg = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e)  { btn.setBackground(hoverBg); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)   { btn.setBackground(BG_PRIMARY); }
            @Override public void mousePressed(java.awt.event.MouseEvent e)  { btn.setBackground(pressBg); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { btn.setBackground(hoverBg); }
        });
        return btn;
    }

    // ── Arama Alanı ──────────────────────────────────────────────────────────

    public static JTextField searchField(String placeholder) {
        JTextField tf = new JTextField(20);
        tf.setFont(vt(16f));
        tf.setForeground(TEXT_PRIMARY);
        tf.setBackground(BG_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);

        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT, 2),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 2),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
        return tf;
    }

    // ── Panel/Frame Fabrikaları ───────────────────────────────────────────────

    public static JPanel pixelPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER, 2), "> " + title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                pixel(7f), ACCENT2
        ));
        return p;
    }

    public static void styleFrame(JFrame frame) {
        frame.setTitle("inventory.io — Demirbaş Yönetim Sistemi");
        frame.getContentPane().setBackground(BG_PRIMARY);
        frame.getRootPane().putClientProperty("JRootPane.titleBarBackground", BG_SURFACE);
        frame.getRootPane().putClientProperty("JRootPane.titleBarForeground", ACCENT);
    }
}