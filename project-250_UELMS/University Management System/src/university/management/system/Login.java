

package university.management.system;

import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Optional;

public class Login extends JFrame implements ActionListener {

    private final String roleHint;
    private JButton login, cancel, forget;
    private JTextField tfusername;
    private JPasswordField tfpassword;

    public Login() {
        this(null);
    }

    public Login(String role) {
        this.roleHint = role == null ? null : role.trim().toUpperCase();
        buildUi();
        // Initialize database tables in background thread (non-blocking)
        new Thread(() -> {
            try {
                DatabaseInitializer.initializeDatabase();
                AuthService.ensureAuthTables();
            } catch (SQLException ex) {
                ex.printStackTrace(); // Log to console
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Database initialization warning:\n" + ex.getMessage() + 
                            "\n\nSome features may not work until this is resolved.",
                            "Database Warning",
                            JOptionPane.WARNING_MESSAGE);
                });
            } catch (Exception ex) {
                ex.printStackTrace(); // Log to console
                // Don't show dialog for unexpected errors to avoid blocking
            }
        }, "DatabaseInitThread").start();
    }

    private void buildUi() {
        // Apply consistent UI theme
        UITheme.applyFrame(this);
        setLayout(null);
        setTitle(roleHint == null ? "Login" : roleHint + " Login");
        setSize(620, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel heading = new JLabel(roleHint == null ? "Sign in to continue"
                : ("Sign in as " + roleHint));
        UITheme.styleTitle(heading);
        heading.setBounds(40, 20, 360, 30);
        add(heading);

        JLabel lblusername = new JLabel(roleHint != null && roleHint.equals("STUDENT")
                ? "Registration / Username"
                : "Username");
        UITheme.styleLabel(lblusername);
        lblusername.setBounds(40, 70, 200, 24);
        add(lblusername);

        tfusername = new JTextField();
        UITheme.styleField(tfusername);
        tfusername.setBounds(40, 95, 260, 36);
        add(tfusername);

        JLabel lblpassword = new JLabel("Password");
        UITheme.styleLabel(lblpassword);
        lblpassword.setBounds(40, 145, 200, 24);
        add(lblpassword);

        tfpassword = new JPasswordField();
        UITheme.styleField(tfpassword);
        tfpassword.setBounds(40, 170, 260, 36);
        add(tfpassword);

        login = new JButton("Login");
        UITheme.stylePrimary(login);
        login.setBounds(40, 220, 120, 36);
        login.addActionListener(this);
        add(login);

        cancel = new JButton("Back");
        UITheme.styleGhost(cancel);
        cancel.setBounds(180, 220, 120, 36);
        cancel.addActionListener(this);
        add(cancel);

        forget = new JButton("Forgot Password");
        UITheme.styleDanger(forget);
        forget.setBounds(90, 270, 160, 30);
        forget.addActionListener(this);
        add(forget);

        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("university/management/system/icons/i2.png"));
        Image i2 = i1.getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel image = new JLabel(i3);
        image.setBounds(340, 50, 220, 200);
        add(image);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == login) {
            performLogin();
        } else if (ae.getSource() == cancel) {
            setVisible(false);
            if (roleHint != null) {
                new AuthChoice(roleHint);
            } else {
                new RoleSelect();
            }
        } else if (ae.getSource() == forget) {
            setVisible(false);
            new Forget_pass(roleHint);
        }
    }

    private void performLogin() {
        String identifier = tfusername.getText() == null ? "" : tfusername.getText().trim();
        String password = new String(tfpassword.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill both fields before logging in.");
            return;
        }

        try {
            Optional<AuthService.AuthResult> result = AuthService.authenticate(identifier, password, roleHint);
            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Invalid credentials or inactive account.", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            AuthService.AuthResult auth = result.get();
            setVisible(false);
            if (auth.isStudent()) {
                new StudentDashboard(auth.displayName());
            } else {
                new Project(auth.getRole()); // Pass user role to Project
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new RoleSelect();
    }
}



















