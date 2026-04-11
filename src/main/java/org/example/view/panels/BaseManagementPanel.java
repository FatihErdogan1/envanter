package org.example.view.panels;

import org.example.util.InventorTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * inventor.io — BaseManagementPanel
 * Ortak: header+arama barı, tablo, form wrapper, buton satırı.
 * FIX: addFormRow null-safe, arama/filtreleme desteği eklendi.
 */
public abstract class BaseManagementPanel extends JPanel {

    protected JTable table;
    protected DefaultTableModel tableModel;
    protected TableRowSorter<DefaultTableModel> sorter;  // arama için

    public BaseManagementPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(InventorTheme.BG_PRIMARY);
    }

    // ── Header + Arama Barı ──────────────────────────────────────────────────
    // FIX: Arama çubuğu header'a entegre edildi.
    // searchCols: hangi sütun indekslerinde arama yapılacak (boş = hepsi)

    protected JPanel buildHeaderBar(String title, int... searchCols) {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(InventorTheme.BG_SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, InventorTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel lbl = new JLabel("▶  " + title);
        lbl.setFont(InventorTheme.pixel(9f));
        lbl.setForeground(InventorTheme.ACCENT);
        bar.add(lbl, BorderLayout.WEST);

        // Arama paneli (sağ taraf)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(InventorTheme.BG_SURFACE);

        JLabel searchIcon = new JLabel("⌕");
        searchIcon.setFont(InventorTheme.pixel(10f));
        searchIcon.setForeground(InventorTheme.COL_MUTED);

        JTextField searchField = InventorTheme.searchField("Ara...");
        searchField.setPreferredSize(new Dimension(220, 36));

        JButton btnClearSearch = InventorTheme.clearButton("✕");
        btnClearSearch.setToolTipText("Aramayı temizle");

        searchPanel.add(searchIcon);
        searchPanel.add(searchField);
        searchPanel.add(btnClearSearch);
        bar.add(searchPanel, BorderLayout.EAST);

        // Arama listener — gerçek zamanlı filtreleme
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { doFilter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { doFilter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { doFilter(); }

            void doFilter() {
                String text = searchField.getText().trim();
                if (sorter == null) return;
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    // searchCols belirtilmişse sadece o sütunlarda ara
                    try {
                        if (searchCols.length == 0) {
                            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                        } else {
                            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, searchCols));
                        }
                    } catch (java.util.regex.PatternSyntaxException ex) {
                        // geçersiz regex girişini yoksay
                    }
                }
            }
        });

        btnClearSearch.addActionListener(e -> {
            searchField.setText("");
            if (sorter != null) sorter.setRowFilter(null);
        });

        return bar;
    }

    // ── Tablo Kurulumu (sorter ataması burada) ────────────────────────────────

    protected void setupSorter() {
        if (tableModel != null && table != null) {
            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);
        }
    }

    // ── Tablo ScrollPane ─────────────────────────────────────────────────────

    protected JScrollPane buildTableScrollPane() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(InventorTheme.BG_PRIMARY);
        scroll.getViewport().setBackground(InventorTheme.BG_PRIMARY);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, InventorTheme.BORDER));
        return scroll;
    }

    // ── Form Wrapper ─────────────────────────────────────────────────────────

    protected JPanel buildFormWrapper(String sectionTitle, JPanel formContent, JPanel btnRow) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setBackground(InventorTheme.BG_SURFACE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, InventorTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 16, 14, 16)
        ));

        JLabel title = new JLabel("> " + sectionTitle);
        title.setFont(InventorTheme.pixel(7f));
        title.setForeground(InventorTheme.ACCENT2);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        wrapper.add(title,       BorderLayout.NORTH);
        wrapper.add(formContent, BorderLayout.CENTER);
        wrapper.add(btnRow,      BorderLayout.SOUTH);
        return wrapper;
    }

    // ── Form Grid ─────────────────────────────────────────────────────────────

    protected JPanel buildFormGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(InventorTheme.BG_SURFACE);
        return grid;
    }

    // ── Buton Satırı ─────────────────────────────────────────────────────────

    protected JPanel buildButtonRow(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        row.setBackground(InventorTheme.BG_SURFACE);
        for (JButton btn : buttons) row.add(btn);
        return row;
    }

    // ── Bileşen Fabrikaları ───────────────────────────────────────────────────

    public static JLabel pxLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(InventorTheme.pixel(7f));
        lbl.setForeground(InventorTheme.COL_MUTED);
        return lbl;
    }

    public static JTextField pxField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(InventorTheme.vt(16f));
        tf.setForeground(InventorTheme.TEXT_PRIMARY);
        tf.setBackground(InventorTheme.BG_PRIMARY);
        tf.setCaretColor(InventorTheme.ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(InventorTheme.BORDER, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        if (placeholder != null && !placeholder.isEmpty()) {
            tf.putClientProperty(
                    com.formdev.flatlaf.FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        }
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(InventorTheme.ACCENT, 2),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(InventorTheme.BORDER, 2),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
        return tf;
    }

    public static <T> JComboBox<T> pxCombo() {
        JComboBox<T> cb = new JComboBox<>();
        cb.setFont(InventorTheme.vt(16f));
        cb.setForeground(InventorTheme.TEXT_PRIMARY);
        cb.setBackground(InventorTheme.BG_PRIMARY);
        cb.setBorder(BorderFactory.createLineBorder(InventorTheme.BORDER, 2));
        return cb;
    }

    // ── GBC yardımcısı ───────────────────────────────────────────────────────

    protected GridBagConstraints gbc(int x, int y, double wx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.weightx = wx;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 8, 5, 8);
        return g;
    }

    // FIX: null-safe iki sütunlu satır
    protected void addFormRow(JPanel grid, int row,
                              String lbl1, JComponent f1, String lbl2, JComponent f2) {
        grid.add(pxLabel(lbl1), gbc(0, row, 0));
        grid.add(f1,            gbc(1, row, 1));
        if (lbl2 != null && f2 != null) {
            grid.add(pxLabel(lbl2), gbc(2, row, 0));
            grid.add(f2,            gbc(3, row, 1));
        }
    }

    // Geniş (4 sütun yayılan) satır
    protected void addFormRowWide(JPanel grid, int row, String lbl, JComponent f) {
        grid.add(pxLabel(lbl), gbc(0, row, 0));
        GridBagConstraints g = gbc(1, row, 1);
        g.gridwidth = 3;
        grid.add(f, g);
    }

    // ── Ortak Dialog ─────────────────────────────────────────────────────────

    protected void showMsg(String msg, Color color) {
        UIManager.put("OptionPane.messageForeground", color);
        JOptionPane.showMessageDialog(this, msg, "inventor.io", JOptionPane.PLAIN_MESSAGE);
    }

    protected boolean confirmDialog(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "⚠  ONAY",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}