package university.management.system;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterAdmin extends JFrame implements ActionListener {
    private JTextField tfUser;
    private JPasswordField tfPass, tfPass2;
    private JButton btnSubmit, btnCancel;

    public RegisterAdmin() {
        setTitle("Admin Registration");
        setSize(680, 340);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Admin Registration");
        UITheme.styleTitle(heading);
        heading.setBounds(0, 20, 680, 30);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        add(heading);

        JLabel lblUser = new JLabel("Username");
        UITheme.styleLabel(lblUser);
        lblUser.setBounds(60, 90, 160, 24);
        add(lblUser);

        tfUser = new JTextField();
        UITheme.styleField(tfUser);
        tfUser.setBounds(240, 88, 260, 36);
        add(tfUser);

        JLabel lblPass = new JLabel("Password");
        UITheme.styleLabel(lblPass);
        lblPass.setBounds(60, 140, 160, 24);
        add(lblPass);

        tfPass = new JPasswordField();
        UITheme.styleField(tfPass);
        tfPass.setBounds(240, 138, 260, 36);
        add(tfPass);

        JLabel lblPass2 = new JLabel("Confirm Password");
        UITheme.styleLabel(lblPass2);
        lblPass2.setBounds(60, 190, 160, 24);
        add(lblPass2);

        tfPass2 = new JPasswordField();
        UITheme.styleField(tfPass2);
        tfPass2.setBounds(240, 188, 260, 36);
        add(tfPass2);

        btnSubmit = new JButton("Register");
        UITheme.stylePrimary(btnSubmit);
        btnSubmit.addActionListener(this);
        btnSubmit.setBounds(240, 240, 140, 36);
        add(btnSubmit);

        btnCancel = new JButton("Cancel");
        UITheme.styleGhost(btnCancel);
        btnCancel.addActionListener(this);
        btnCancel.setBounds(400, 240, 140, 36);
        add(btnCancel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCancel) {
            setVisible(false);
            new AuthChoice("ADMIN");
            return;
        }
        if (e.getSource() == btnSubmit) {
            String u = tfUser.getText() == null ? "" : tfUser.getText().trim();
            String p1 = new String(tfPass.getPassword());
            String p2 = new String(tfPass2.getPassword());
            if (u.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required");
                return;
            }
            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match");
                return;
            }
            try {
                AuthService.ensureAuthTables();
                try (Conn c = new Conn()) {
                    if (!c.isConnected()) {
                        JOptionPane.showMessageDialog(this, "Database connection failed. Please check Railway credentials.");
                        return;
                    }
                    try (PreparedStatement ps = c.c.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
                        ps.setString(1, u);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                JOptionPane.showMessageDialog(this, "Username already exists. Please login.");
                                return;
                            }
                        }
                    }
                    try (PreparedStatement ps = c.c.prepareStatement(
                            "INSERT INTO users (username, password, role, is_active) VALUES (?, ?, 'ADMIN', 1)")) {
                        ps.setString(1, u);
                        ps.setString(2, p1);
                        ps.executeUpdate();
                    }
                }
                JOptionPane.showMessageDialog(this, "Admin registered. Please log in.");
                setVisible(false);
                new Login("ADMIN");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage());
            }
        }
    }
}
