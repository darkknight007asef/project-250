package university.management.system.ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import university.management.system.AuthService;
import university.management.system.Conn;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ForgetView {
    private final JPanel root = new JPanel(new BorderLayout());
    private final JFrame frame;

    public ForgetView(JFrame frame) {
        this.frame = frame;

        JPanel container = new JPanel(new GridBagLayout());
        container.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Password Recovery");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        container.add(title, gbc);

        gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel emailLbl = new JLabel("Registered Email");
        JTextField email = new JTextField();
        gbc.gridy = 1; gbc.gridx = 0; container.add(emailLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1; container.add(email, gbc);

        JButton send = new JButton("Show Credentials");
        JButton back = new JButton("Back");
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 0; gbc.anchor = GridBagConstraints.CENTER;
        JPanel actions = new JPanel();
        actions.add(send);
        actions.add(back);
        container.add(actions, gbc);

        root.add(container, BorderLayout.CENTER);

        back.addActionListener(e -> SceneFactory.showDashboard(frame));
        send.addActionListener(e -> lookup(email.getText()));
    }

    private void lookup(String email) {
        String value = email == null ? "" : email.trim();
        if (value.isEmpty()) {
            Dialogs.error("Please enter a registered email.");
            return;
        }

        try {
            AuthService.ensureAuthTables();
            try (Conn c = new Conn()) {
                if (!c.isConnected()) {
                    Dialogs.error("Database connection unavailable.");
                    return;
                }
                String sql = "SELECT u.username, u.registration_no, u.password " +
                        "FROM users u LEFT JOIN student s ON u.registration_no = s.registration_no " +
                        "WHERE (s.email = ? OR u.username = ?) AND u.is_active = 1 LIMIT 1";
                try (PreparedStatement ps = c.c.prepareStatement(sql)) {
                    ps.setString(1, value);
                    ps.setString(2, value);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Dialogs.info("User: " + rs.getString("username") +
                                    "\nReg: " + rs.getString("registration_no") +
                                    "\nPassword: " + rs.getString("password"));
                        } else {
                            Dialogs.error("No account found for " + value);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Dialogs.error("Failed: " + ex.getMessage());
        }
    }

    public JPanel getRoot() { return root; }
}


