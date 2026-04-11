package org.example.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * inventor.io — TableHelper
 * Tabloları pixel dark tema ile stillendirir.
 * AssetManagementPanel ve diğer panellerde kullanılır.
 */
public class TableHelper {

    /**
     * JTable'a tam inventor.io stilini uygular.
     * Mevcut kullanım: TableHelper.setupTable(assetTable);
     */
    public static void setupTable(JTable table) {
        // Temel görünüm
        table.setBackground(InventorTheme.BG_PRIMARY);
        table.setForeground(InventorTheme.TEXT_PRIMARY);
        table.setFont(InventorTheme.vt(16f));
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(InventorTheme.BORDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setSelectionBackground(new Color(0x00FFCC22, true));
        table.setSelectionForeground(InventorTheme.ACCENT);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);

        // Alternatif satır rengi
        table.putClientProperty("FlatLaf.style",
                "alternateRowColor: #13132B");

        // Başlık (header)
        JTableHeader header = table.getTableHeader();
        header.setBackground(InventorTheme.BG_SURFACE);
        header.setForeground(InventorTheme.ACCENT);
        header.setFont(InventorTheme.pixel(7f));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, InventorTheme.ACCENT));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        // Hücre render'ı — ID sütunu özel stil
        table.getColumnModel().getColumn(0).setCellRenderer(new IdCellRenderer());

        // Durum sütunu varsa (son sütun) badge render
        int lastCol = table.getColumnCount() - 1;
        table.getColumnModel().getColumn(lastCol).setCellRenderer(new StatusBadgeRenderer());

        // Sütun genişlikleri
        int[] widths = recommendedWidths(table.getColumnCount());
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /** ID sütunu için VT323 font + soluk ama okunabilir renk */
    static class IdCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                                                       boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            setFont(InventorTheme.vt(15f));
            setForeground(sel ? InventorTheme.ACCENT : InventorTheme.TEXT_PRIMARY);
            setBackground(sel ? new Color(0x00FFCC22, true) : InventorTheme.BG_PRIMARY);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, InventorTheme.BORDER));
            setText(val != null ? "#" + String.format("%04d", val) : "");
            setHorizontalAlignment(CENTER);
            return this;
        }
    }

    /** Durum (Status) sütunu için renkli badge */
    static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                                                       boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);

            String status = val != null ? val.toString() : "";
            Color fg = statusColor(status);
            String label = statusLabel(status);

            setFont(InventorTheme.vt(15f));
            setForeground(fg);
            setBackground(sel ? new Color(0x00FFCC22, true) : InventorTheme.BG_PRIMARY);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, InventorTheme.BORDER),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            setText("[ " + label + " ]");
            setHorizontalAlignment(CENTER);
            return this;
        }

        private Color statusColor(String status) {
            return switch (status.toUpperCase()) {
                case "ACTIVE"      -> InventorTheme.COL_GREEN;
                case "MAINTENANCE" -> InventorTheme.COL_YELLOW;
                case "RETIRED"     -> InventorTheme.COL_RED;
                case "RESERVED"    -> InventorTheme.COL_BLUE;
                default            -> InventorTheme.COL_MUTED;
            };
        }

        private String statusLabel(String status) {
            return switch (status.toUpperCase()) {
                case "ACTIVE"      -> "AKTİF";
                case "MAINTENANCE" -> "BAKIM";
                case "RETIRED"     -> "HURDA";
                case "RESERVED"    -> "REZERVE";
                default            -> status;
            };
        }
    }

    /** Sütun sayısına göre önerilen genişlikler */
    private static int[] recommendedWidths(int colCount) {
        return switch (colCount) {
            case 7 -> new int[]{60, 110, 200, 130, 130, 100, 90};
            case 5 -> new int[]{60, 160, 200, 130, 100};
            default -> {
                int[] arr = new int[colCount];
                java.util.Arrays.fill(arr, 120);
                yield arr;
            }
        };
    }
}