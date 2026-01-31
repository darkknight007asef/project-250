package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AuthChoice extends JFrame implements ActionListener {
    private final String role; // "ADMIN", "TEACHER", or "STUDENT"
    private JButton registerBtn, loginBtn, backBtn;

    public AuthChoice(String role) {
        this.role = role;
        setTitle("Authentication - " + role);
        setSize(520, 300);
        setLocationRelativeTo(null);
        setLayout(null);
        UITheme.applyFrame(this);

        String headingText = "Access";
        if ("ADMIN".equals(role)) {
            headingText = "Admin Access";
        } else if ("TEACHER".equals(role)) {
            headingText = "Teacher Access";
        } else {
            headingText = "Student Access";
        }
        JLabel heading = new JLabel(headingText);
        UITheme.styleTitle(heading);
        heading.setBounds(0, 20, 520, 30);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        add(heading);

        // Helper subtext
        JLabel helper = new JLabel("If you already have an account, log in below");
        UITheme.styleLabel(helper);
        helper.setBounds(0, 58, 520, 22);
        helper.setHorizontalAlignment(SwingConstants.CENTER);
        add(helper);

        registerBtn = new JButton("Register");
        UITheme.stylePrimary(registerBtn);
        // Larger Register button (clear CTA)
        registerBtn.setBounds(70, 140, 180, 40);
        registerBtn.addActionListener(this);
        add(registerBtn);

        loginBtn = new JButton("Login");
        UITheme.styleGhost(loginBtn);
        // Slightly smaller Login button
        loginBtn.setBounds(290, 144, 140, 36);
        loginBtn.addActionListener(this);
        add(loginBtn);

        backBtn = new JButton("Back");
        UITheme.styleGhost(backBtn);
        backBtn.setBounds(20, 16, 80, 30);
        backBtn.addActionListener(this);
        add(backBtn);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backBtn) {
            setVisible(false);
            new RoleSelect();
            return;
        }
        if (e.getSource() == registerBtn) {
            setVisible(false);
            if ("ADMIN".equals(role)) {
                new RegisterAdmin();
            } else if ("TEACHER".equals(role)) {
                new RegisterTeacher();
            } else {
                new RegisterStudent();
            }
        } else if (e.getSource() == loginBtn) {
            setVisible(false);
            new Login(role);
        }
    }
}
