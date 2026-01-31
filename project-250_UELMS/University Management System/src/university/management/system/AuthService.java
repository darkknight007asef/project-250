package university.management.system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Optional;

/**
 * Centralised authentication helper shared by Swing and modern UI flows.
 * It bootstraps tables on Railway the first time it is used so that every
 * packaged JAR can run without manual SQL steps.
 */
public final class AuthService {

    private static final Object TABLE_LOCK = new Object();
    private static volatile boolean tablesReady = false;

    private AuthService() {}

    public static final class AuthResult {
        private final int userId;
        private final String username;
        private final String registrationNo;
        private final String role;

        private AuthResult(int userId, String username, String registrationNo, String role) {
            this.userId = userId;
            this.username = username;
            this.registrationNo = registrationNo;
            this.role = role;
        }

        public int getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRegistrationNo() {
            return registrationNo;
        }

        public String getRole() {
            return role;
        }

        public boolean isStudent() {
            return "STUDENT".equalsIgnoreCase(role);
        }

        public String displayName() {
            if (registrationNo != null && !registrationNo.isBlank()) {
                return registrationNo;
            }
            return username;
        }
    }

    public static Optional<AuthResult> authenticate(String rawIdentifier, String rawPassword, String roleHint) throws SQLException {
        String identifier = rawIdentifier == null ? "" : rawIdentifier.trim();
        String password = rawPassword == null ? "" : rawPassword.trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            return Optional.empty();
        }

        ensureAuthTables();

        String normalizedRole = (roleHint == null || roleHint.trim().isEmpty())
                ? null
                : roleHint.trim().toUpperCase(Locale.ROOT);

        try (Conn conn = new Conn()) {
            if (!conn.isConnected()) {
                throw new SQLException("Database connection is not available.");
            }

            StringBuilder sql = new StringBuilder(
                    "SELECT id, username, registration_no, role " +
                    "FROM users WHERE is_active = 1 AND password = ? " +
                    "AND (username = ? OR registration_no = ?)");
            if (normalizedRole != null) {
                sql.append(" AND role = ?");
            }
            sql.append(" LIMIT 1");

            try (PreparedStatement ps = conn.c.prepareStatement(sql.toString())) {
                ps.setString(1, password);
                ps.setString(2, identifier);
                ps.setString(3, identifier);
                if (normalizedRole != null) {
                    ps.setString(4, normalizedRole);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new AuthResult(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("registration_no"),
                                rs.getString("role")
                        ));
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static void ensureAuthTables() throws SQLException {
        if (tablesReady) {
            return;
        }
        synchronized (TABLE_LOCK) {
            if (tablesReady) {
                return;
            }
            try (Conn conn = new Conn()) {
                if (!conn.isConnected()) {
                    throw new SQLException("Unable to initialize authentication tables; connection failed.");
                }
                createTables(conn.c);
                seedDefaultAdmin(conn.c);
            }
            tablesReady = true;
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "username VARCHAR(50) DEFAULT NULL," +
                    "registration_no VARCHAR(20) DEFAULT NULL," +
                    "password VARCHAR(100) NOT NULL," +
                    "role VARCHAR(10) NOT NULL," +
                    "is_active TINYINT(1) NOT NULL DEFAULT 1," +
                    "PRIMARY KEY (id)," +
                    "UNIQUE KEY uk_username (username)," +
                    "UNIQUE KEY uk_registration_no (registration_no))");

            st.execute("CREATE TABLE IF NOT EXISTS forget_pass (" +
                    "email VARCHAR(100) DEFAULT NULL," +
                    "username VARCHAR(100) DEFAULT NULL," +
                    "password VARCHAR(100) DEFAULT NULL)");
        }
    }

    private static void seedDefaultAdmin(Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (username, password, role, is_active) " +
                "SELECT ?, ?, 'ADMIN', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = ?)")) {
            ps.setString(1, "admin");
            ps.setString(2, "admin123");
            ps.setString(3, "admin");
            ps.executeUpdate();
        } catch (SQLException ignored) {
            // Ignore duplicate errors; only want to guarantee at least one admin
        }
    }

    public static boolean recordPasswordRequest(String email, String username, String password) throws SQLException {
        ensureAuthTables();
        try (Conn conn = new Conn()) {
            if (!conn.isConnected()) {
                throw new SQLException("Database connection is not available.");
            }
            try (PreparedStatement ps = conn.c.prepareStatement(
                    "INSERT INTO forget_pass (email, username, password) VALUES (?, ?, ?)")) {
                ps.setString(1, email);
                ps.setString(2, username);
                ps.setString(3, password);
                return ps.executeUpdate() > 0;
            }
        }
    }
}


