package university.management.system;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Forget_pass extends JFrame implements ActionListener {

    private static final String DEFAULT_RESET_EMAIL = "darkknight5812@gmail.com";

    private final String roleHint;
    private JTextField tfIdentifier;
    private JButton sendBtn;
    private JButton cancelBtn;

    public Forget_pass() {
        this(null);
    }

    public Forget_pass(String roleHint) {
        this.roleHint = roleHint == null ? null : roleHint.trim().toUpperCase();
        buildUi();
    }

    private void buildUi() {
        UITheme.applyFrame(this);
        setLayout(null);

        JLabel heading = new JLabel("Recover Password");
        UITheme.styleTitle(heading);
        heading.setBounds(40, 10, 420, 28);
        add(heading);

        JLabel lbl = new JLabel("Enter your username / registration no:");
        UITheme.styleLabel(lbl);
        lbl.setBounds(40, 60, 360, 24);
        add(lbl);

        tfIdentifier = new JTextField();
        UITheme.styleField(tfIdentifier);
        tfIdentifier.setBounds(40, 90, 320, 34);
        add(tfIdentifier);

        sendBtn = new JButton("Send Request");
        UITheme.stylePrimary(sendBtn);
        sendBtn.setBounds(380, 90, 150, 34);
        sendBtn.addActionListener(this);
        add(sendBtn);

        cancelBtn = new JButton("Cancel");
        UITheme.styleGhost(cancelBtn);
        cancelBtn.setBounds(540, 90, 100, 34);
        cancelBtn.addActionListener(this);
        add(cancelBtn);

        JLabel note = new JLabel("Note: Requests are logged to " + DEFAULT_RESET_EMAIL + " for admin follow-up.");
        UITheme.styleLabel(note);
        note.setBounds(40, 140, 620, 22);
        add(note);

        setSize(720, 220);
        setLocation(480, 320);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendBtn) {
            submitRequest();
        } else if (e.getSource() == cancelBtn) {
            setVisible(false);
            if (roleHint != null) {
                new Login(roleHint);
            } else {
                new RoleSelect();
            }
        }
    }

    private void submitRequest() {
        String identifier = tfIdentifier.getText() == null ? "" : tfIdentifier.getText().trim();
        if (identifier.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username or registration number.");
            return;
        }

        try {
            AuthService.ensureAuthTables();
            String password = lookupPassword(identifier);
            if (password == null) {
                JOptionPane.showMessageDialog(this, "No active account found for " + identifier,
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean recorded = AuthService.recordPasswordRequest(DEFAULT_RESET_EMAIL, identifier, password);
            if (recorded) {
                JOptionPane.showMessageDialog(this, "Password request submitted. Please check your registered email soon.");
                setVisible(false);
                if (roleHint != null) {
                    new Login(roleHint);
                } else {
                    new RoleSelect();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Could not log the request. Contact administrator.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to submit request: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String lookupPassword(String identifier) throws SQLException {
        try (Conn conn = new Conn()) {
            if (!conn.isConnected()) {
                throw new SQLException("Database connection is not available.");
            }
            try (PreparedStatement ps = conn.c.prepareStatement(
                    "SELECT password FROM users WHERE is_active = 1 AND (username = ? OR registration_no = ?)")) {
                ps.setString(1, identifier);
                ps.setString(2, identifier);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("password");
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        new Forget_pass();
    }
}


