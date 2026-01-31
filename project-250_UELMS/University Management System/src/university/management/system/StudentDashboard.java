package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.sql.*;

public class StudentDashboard extends JFrame {
    private final String registrationNo;

    public StudentDashboard(String registrationNo) {
        this.registrationNo = registrationNo;
        setTitle("Student Dashboard");
        setSize(800, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        UITheme.applyFrame(this);

        String displayName = registrationNo;
        try {
            Conn c = new Conn();
            String query = "SELECT name FROM student WHERE registration_no = '" + registrationNo + "'";
            ResultSet rs = c.s.executeQuery(query);
            if (rs.next()) {
                displayName = rs.getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel heading = new JLabel("Welcome, " + displayName, SwingConstants.CENTER);
        UITheme.styleTitle(heading);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 0, 6, 0));
        add(heading, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 100, 40, 100));

        grid.add(createCard("📑", "View Marks", () -> new ViewMarks(this.registrationNo)));
        grid.add(createCard("📊", "View Results", () -> new StudentResultView(this.registrationNo)));
        grid.add(createCard("🎓", "Certificate", () -> new CertificateView(this.registrationNo)));
        grid.add(createCard("🔄", "Recheck", () -> new StudentRecheckRequestView(this.registrationNo)));

        add(grid, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createCard(String icon, String title, Runnable onClick) {
        JPanel card = UITheme.cardPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(220, 140));

        // Icon label
        JLabel iconLabel = new JLabel(icon, JLabel.CENTER) {
            @Override
            public Dimension getPreferredSize() { return new Dimension(40, 40); }
        };
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLabel.setForeground(UITheme.ACCENT_PINK);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Click to open", JLabel.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(Box.createVerticalGlue());
        content.add(iconLabel);
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(titleLabel);
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(subtitle);
        content.add(Box.createVerticalGlue());

        card.add(content, BorderLayout.CENTER);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final Color original = UITheme.CARD_DARK;
        final Color hover = new Color(236, 232, 254);
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { card.setBackground(original); }
        });
        return card;
    }
}
