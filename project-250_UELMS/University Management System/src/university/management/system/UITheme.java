package university.management.system;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.basic.BasicButtonUI;

public final class UITheme {
    private UITheme() {}

    public static final Color BG_DARK = Color.WHITE; // pure white background as requested
    public static final Color CARD_DARK = Color.WHITE; // card/background surfaces (white)
    public static final Color CARD_BORDER = new Color(136, 84, 208); // kept for table grid if needed
    public static final Color TEXT_PRIMARY = new Color(20, 20, 22); // near-black for contrast on white
    public static final Color TEXT_SECONDARY = new Color(120, 124, 135); // soft gray
    public static final Color ACCENT_PINK = new Color(124, 58, 237); // purple brand
    public static final Color DANGER = new Color(224, 77, 90);
    private static final Color BORDER_LIGHT = new Color(230, 232, 240); // subtle gray outline
    private static final Color HOVER_PURPLE_BG = new Color(236, 232, 254);
    // drop shadow removed per request

    public static void applyFrame(JFrame frame) {
        frame.getContentPane().setBackground(BG_DARK);
    }

    public static JPanel cardPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        styleCard(p);
        return p;
    }

    public static void styleCard(JPanel panel) {
        panel.setBackground(CARD_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
    }

    public static void styleTitle(JLabel label) {
        label.setForeground(TEXT_PRIMARY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
    }

    public static void styleLabel(JLabel label) {
        label.setForeground(TEXT_PRIMARY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    public static void styleField(JTextField field) {
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        field.setCaretColor(Color.BLACK);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_PINK, 1, true),
                        new EmptyBorder(6, 10, 6, 10)));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                        new EmptyBorder(6, 10, 6, 10)));
            }
        });
    }

    public static void styleField(JPasswordField field) {
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        field.setCaretColor(Color.BLACK);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_PINK, 1, true),
                        new EmptyBorder(6, 10, 6, 10)));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                        new EmptyBorder(6, 10, 6, 10)));
            }
        });
    }

    public static void stylePrimary(JButton b) {
        b.setBackground(ACCENT_PINK);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setBorder(new EmptyBorder(8, 18, 8, 18)); // clean, no shadow
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Ensure the purple fill shows on all LAFs
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBorderPainted(true);
        b.setUI(new BasicButtonUI());
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(ACCENT_PINK.brighter());
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(ACCENT_PINK);
            }
            public void mousePressed(MouseEvent e) {
                b.setBackground(ACCENT_PINK.darker());
            }
            public void mouseReleased(MouseEvent e) {
                b.setBackground(ACCENT_PINK.brighter());
            }
        });
    }

    public static void styleGhost(JButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(ACCENT_PINK);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 16, 8, 16)));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Ensure OS LAF doesn't draw gray gradient fill
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(HOVER_PURPLE_BG);
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(Color.WHITE);
            }
            public void mousePressed(MouseEvent e) {
                b.setBackground(ACCENT_PINK);
                b.setForeground(Color.WHITE);
            }
            public void mouseReleased(MouseEvent e) {
                b.setBackground(HOVER_PURPLE_BG);
                b.setForeground(ACCENT_PINK);
            }
        });
    }

    public static void styleSecondary(JButton b) {
        b.setBackground(new Color(10, 10, 10)); // Dark Gray/Black
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setUI(new BasicButtonUI());
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(Color.BLACK); }
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(30, 30, 30)); }
        });
    }

    public static void styleDanger(JButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(224, 77, 90));
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14)); // no border
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleTable(JTable table) {
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(238, 238, 242)); // subtle light gray grid
        table.setSelectionBackground(new Color(236, 232, 254)); // light purple selection
        table.setSelectionForeground(TEXT_PRIMARY); // keep text dark on selection
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setBackground(Color.WHITE);
            header.setForeground(TEXT_PRIMARY);
            header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }
    }

    public static void styleComboPurple(JComboBox<?> combo) {
        combo.setBackground(ACCENT_PINK);
        combo.setForeground(Color.WHITE);
        combo.setOpaque(true);
        combo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        combo.setBorder(new EmptyBorder(4, 10, 4, 10));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setForeground(Color.WHITE);
                c.setBackground(isSelected ? ACCENT_PINK.darker() : ACCENT_PINK);
                return c;
            }
        });

        if (combo.isEditable()) {
            Component editor = combo.getEditor().getEditorComponent();
            if (editor instanceof JTextField) {
                JTextField tf = (JTextField) editor;
                tf.setBackground(ACCENT_PINK);
                tf.setForeground(Color.WHITE);
                tf.setCaretColor(Color.WHITE);
                tf.setBorder(new EmptyBorder(4, 10, 4, 10));
            }
        }
    }

    public static void styleScroll(JScrollPane sp) {
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(new EmptyBorder(0, 0, 0, 0)); // no border
    }
}

