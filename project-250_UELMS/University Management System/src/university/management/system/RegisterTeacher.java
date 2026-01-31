package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterTeacher extends JFrame implements ActionListener {
    private JTextField tfEmpId;
    private JPasswordField tfPass, tfPass2;
    private JButton btnSubmit, btnCancel;

    public RegisterTeacher() {
        setTitle("Teacher Registration");
        setSize(680, 360);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);

        JLabel heading = new JLabel("Teacher Registration");
        UITheme.styleTitle(heading);
        heading.setBounds(0, 20, 680, 30);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        add(heading);

        JLabel lblEmpId = new JLabel("Employee ID");
        UITheme.styleLabel(lblEmpId);
        lblEmpId.setBounds(60, 90, 160, 24);
        add(lblEmpId);

        tfEmpId = new JTextField();
        UITheme.styleField(tfEmpId);
        tfEmpId.setBounds(240, 88, 260, 36);
        add(tfEmpId);

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
        btnSubmit.setBounds(240, 250, 140, 36);
        add(btnSubmit);

        btnCancel = new JButton("Cancel");
        UITheme.styleGhost(btnCancel);
        btnCancel.addActionListener(this);
        btnCancel.setBounds(400, 250, 140, 36);
        add(btnCancel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCancel) {
            setVisible(false);
            new AuthChoice("TEACHER");
            return;
        }
        if (e.getSource() == btnSubmit) {
            String empId = tfEmpId.getText() == null ? "" : tfEmpId.getText().trim();
            String p1 = new String(tfPass.getPassword());
            String p2 = new String(tfPass2.getPassword());

            if (empId.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
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
                        JOptionPane.showMessageDialog(this, "Database connection failed.");
                        return;
                    }

                    // 1. Verify Employee ID exists in teacher table
                    try (PreparedStatement ps = c.c.prepareStatement("SELECT 1 FROM teacher WHERE empId=?")) {
                        ps.setString(1, empId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                JOptionPane.showMessageDialog(this, "Employee ID not found. Please contact admin to add your profile first.");
                                return;
                            }
                        }
                    }

                    // 2. Check if account already exists
                    try (PreparedStatement ps = c.c.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
                        ps.setString(1, empId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                JOptionPane.showMessageDialog(this, "Account already exists. Please log in.");
                                return;
                            }
                        }
                    }

                    // 3. Create user account
                    try (PreparedStatement ps = c.c.prepareStatement(
                            "INSERT INTO users (username, password, role, is_active) VALUES (?, ?, 'TEACHER', 1)")) {
                        ps.setString(1, empId);
                        ps.setString(2, p1);
                        ps.executeUpdate();
                    }
                }
                JOptionPane.showMessageDialog(this, "Registration successful. Please log in.");
                setVisible(false);
                new Login("TEACHER");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage());
            }
        }
    }
}
