package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.RenderingHints;
import java.sql.SQLException;

public class RoleSelect extends JFrame implements ActionListener {
    private JComboBox<String> roleCombo;
    private JButton submitBtn, backBtn;

    public RoleSelect() {
        setTitle("Select Role");
        setSize(560, 320);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);
        
        // Initialize database tables on startup in background thread (non-blocking)
        new Thread(() -> {
            try {
                DatabaseInitializer.initializeDatabase();
                AuthService.ensureAuthTables();
            } catch (SQLException e) {
                e.printStackTrace(); // Log to console
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Database initialization warning:\n" + e.getMessage() + 
                            "\n\nSome features may not work until this is resolved.",
                            "Database Warning",
                            JOptionPane.WARNING_MESSAGE);
                });
            } catch (Exception e) {
                e.printStackTrace(); // Log to console
                // Don't show dialog for unexpected errors to avoid blocking
            }
        }, "DatabaseInitThread").start();

        // Header with drawn icon (no emoji to avoid missing glyph)
        JLabel heading = new JLabel("University Management System", JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 20; int x = getWidth()/2 - getFontMetrics(getFont()).stringWidth(getText())/2 - size - 10; int y = getHeight()/2 - size/2;
                g2.setColor(UITheme.ACCENT_PINK);
                g2.fillOval(x, y, size, size);
                g2.dispose();
            }
        };
        UITheme.styleTitle(heading);
        heading.setBounds(0, 24, 560, 32);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(heading);

        // Row: label next to dropdown, both centered
        JLabel sub = new JLabel("Select your role:");
        UITheme.styleLabel(sub);
        sub.setFont(new Font("Segoe UI", Font.BOLD, 14));

        roleCombo = new JComboBox<>(new String[]{"ADMIN", "TEACHER", "STUDENT"});
        roleCombo.setBounds(0, 0, 0, 0); // will position below
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleCombo.setBackground(Color.WHITE);
        // compute centered positions
        int comboW = 220, comboH = 36;
        int gap = 10;
        int subW = 150, subH = 24;
        int totalW = subW + gap + comboW;
        int startX = (560 - totalW) / 2;
        int rowY = 120;
        sub.setBounds(startX, rowY + 6, subW, subH);
        add(sub);
        roleCombo.setBounds(startX + subW + gap, rowY, comboW, comboH);
        add(roleCombo);

        // Submit and Back
        submitBtn = new JButton("Submit");
        UITheme.stylePrimary(submitBtn);
        int btnW = 120, btnH = 36, btnGap = 20;
        int buttonsTotalW = btnW * 2 + btnGap;
        int btnStartX = (560 - buttonsTotalW) / 2;
        int btnY = 185;
        submitBtn.setBounds(btnStartX, btnY, btnW, btnH);
        submitBtn.addActionListener(this);
        add(submitBtn);

        backBtn = new JButton("Back");
        UITheme.styleGhost(backBtn);
        backBtn.setBounds(btnStartX + btnW + btnGap, btnY, btnW, btnH);
        backBtn.addActionListener(this);
        add(backBtn);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitBtn) {
            String selected = (String) roleCombo.getSelectedItem();
            if (selected == null || selected.isEmpty()) return;
            setVisible(false);
            new AuthChoice(selected);
        } else if (e.getSource() == backBtn) {
            // Exit or hide if invoked as the first screen
            setVisible(false);
            dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RoleSelect::new);
    }
}
