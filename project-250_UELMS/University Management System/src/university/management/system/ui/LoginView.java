package university.management.system.ui;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import university.management.system.AuthService;
import university.management.system.StudentDashboard;

import java.sql.SQLException;
import java.util.Optional;

public class LoginView {
    private final JPanel root = new JPanel(new BorderLayout());
    private final JFrame frame;

    public LoginView(JFrame frame) {
        this.frame = frame;

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        container.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = boldLabel("University Management System", 20);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subtitle = plainLabel("Sign in to continue", 13);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints top = new GridBagConstraints();
        top.gridx = 0; top.gridy = 0; top.fill = GridBagConstraints.HORIZONTAL; top.weightx = 1; top.insets = new Insets(0,0,12,0);
        card.add(title, top);
        top.gridy = 1; card.add(subtitle, top);

        JLabel userLbl = plainLabel("Username", 13);
        JTextField username = new JTextField();
        username.setPreferredSize(new Dimension(220, 28));

        JLabel passLbl = plainLabel("Password", 13);
        JPasswordField password = new JPasswordField();
        password.setPreferredSize(new Dimension(220, 28));

        gbc.gridy = 2; gbc.gridx = 0; card.add(userLbl, gbc);
        gbc.gridx = 1; card.add(username, gbc);
        gbc.gridy = 3; gbc.gridx = 0; card.add(passLbl, gbc);
        gbc.gridx = 1; card.add(password, gbc);

        JButton loginBtn = primaryButton("Login");
        JButton cancelBtn = ghostButton("Cancel");
        JButton forgetBtn = ghostButton("Forgot password?");

        GridBagConstraints actions = new GridBagConstraints();
        actions.gridy = 4; actions.gridx = 0; actions.insets = new Insets(12,0,0,0);
        actions.gridwidth = 2; actions.anchor = GridBagConstraints.CENTER;
        JPanel actionRow = new JPanel();
        actionRow.add(loginBtn);
        actionRow.add(cancelBtn);
        actionRow.add(forgetBtn);
        card.add(actionRow, actions);

        container.add(card, new GridBagConstraints());
        root.add(container, BorderLayout.CENTER);

        ActionListener loginAction = e -> doLogin(username.getText(), new String(password.getPassword()));
        loginBtn.addActionListener(loginAction);
        cancelBtn.addActionListener(e -> frame.dispose());
        forgetBtn.addActionListener(e -> SceneFactory.showForget(frame));
    }

    private JLabel boldLabel(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, (float) size));
        return l;
    }

    private JLabel plainLabel(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, (float) size));
        return l;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        return b;
    }

    private JButton ghostButton(String text) {
        JButton b = new JButton(text);
        return b;
    }

    private void doLogin(String user, String pass) {
        String identifier = user == null ? "" : user.trim();
        if (identifier.isEmpty() || pass == null || pass.isEmpty()) {
            Dialogs.error("Please enter both username and password.");
            return;
        }

        try {
            Optional<AuthService.AuthResult> result = AuthService.authenticate(identifier, pass, null);
            if (result.isEmpty()) {
                Dialogs.error("Invalid username or password.");
                return;
            }
            AuthService.AuthResult auth = result.get();
            if (auth.isStudent()) {
                frame.dispose();
                new StudentDashboard(auth.displayName());
            } else {
                SceneFactory.showDashboard(frame);
            }
        } catch (SQLException ex) {
            Dialogs.error("Login failed: " + ex.getMessage());
        }
    }

    public JPanel getRoot() { return root; }
}


