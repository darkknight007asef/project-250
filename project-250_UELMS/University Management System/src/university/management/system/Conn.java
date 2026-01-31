
package university.management.system;

import java.sql.*;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Conn implements AutoCloseable {
    public Connection c;
    public Statement s;
    private boolean initialized = false;
    
    public Conn() {
        c = null;
        s = null;
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Load database configuration from properties file or environment variables
            Properties props = loadDbProperties();
            
            String url = propOrSysEnv(props, "db.url", "DB_URL");
            String user = propOrSysEnv(props, "db.user", "DB_USER");
            String password = propOrSysEnv(props, "db.pass", "DB_PASS");
            
            // Fallback to default localhost if no configuration found
            if (url == null || url.isEmpty()) {
                url = "jdbc:mysql://localhost:3306/universitymanagementsystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                System.out.println("Warning: Using default localhost connection. Configure db.properties for Railway.");
            }
            if (user == null || user.isEmpty()) {
                user = "root";
            }
            if (password == null || password.isEmpty()) {
                password = "1716504726";
            }
            
            // Establish connection
            c = DriverManager.getConnection(url, user, password);
            s = c.createStatement();
            initialized = true;
            
            System.out.println("Database connected successfully!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            String errorMsg = "MySQL JDBC Driver (mysql-connector-java) not found in classpath!\n\n" +
                "To fix this:\n" +
                "1. Download mysql-connector-java-8.0.28.jar\n" +
                "2. Place it in the 'lib' folder of your project\n" +
                "3. Rebuild the project\n" +
                "4. Ensure the JAR is in your classpath when running\n\n" +
                "Alternative: Ensure mysql-connector-java.jar is in the classpath.";
            System.err.println(errorMsg);
            showErrorDialog(errorMsg);
            throw new IllegalStateException("MySQL JDBC Driver not found. Please install mysql-connector-java.jar", e);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            String errorMsg = "Failed to connect to database!\n\nError: " + e.getMessage() + 
                "\n\nPlease check:\n1. Database server is running\n2. db.properties file is configured correctly\n3. Network connection is available\n4. Database credentials are correct\n5. Firewall is not blocking the connection";
            System.err.println(errorMsg);
            showErrorDialog(errorMsg);
            throw new IllegalStateException("Failed to connect to database: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Unexpected error: " + e.getMessage());
            throw new IllegalStateException("Unexpected error creating database connection", e);
        }
    }
    
    private static Properties loadDbProperties() {
        Properties props = new Properties();
        
        // 1) Explicit path via -Ddb.config system property
        String cfgPath = System.getProperty("db.config");
        if (cfgPath != null && !cfgPath.isEmpty()) {
            if (tryLoad(props, cfgPath)) {
                System.out.println("Loaded db.properties from: " + cfgPath);
                return props;
            }
        }
        
        // 2) Next to the running JAR: db.properties
        File jarDir = getJarDir();
        if (jarDir != null) {
            File nextToJar = new File(jarDir, "db.properties");
            if (tryLoad(props, nextToJar.getAbsolutePath())) {
                System.out.println("Loaded db.properties from: " + nextToJar.getAbsolutePath());
                return props;
            }
        }
        
        // 3) In project root directory
        File projectRoot = new File("db.properties");
        if (tryLoad(props, projectRoot.getAbsolutePath())) {
            System.out.println("Loaded db.properties from: " + projectRoot.getAbsolutePath());
            return props;
        }
        
        // 4) User home fallback: ~/.uems/db.properties
        String home = System.getProperty("user.home");
        if (home != null) {
            File homeFile = new File(new File(home, ".uems"), "db.properties");
            if (tryLoad(props, homeFile.getAbsolutePath())) {
                System.out.println("Loaded db.properties from: " + homeFile.getAbsolutePath());
                return props;
            }
        }
        
        // 5) Classpath fallback (for development)
        try (InputStream in = Conn.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
                System.out.println("Loaded db.properties from classpath");
                return props;
            }
        } catch (Exception ignored) {}
        
        return props; // May be empty if no config found
    }
    
    private static boolean tryLoad(Properties props, String path) {
        if (path == null || path.isEmpty()) return false;
        try {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load properties from: " + path);
        }
        return false;
    }
    
    private static String propOrSysEnv(Properties props, String propKey, String envKey) {
        // First try system property
        String value = System.getProperty(propKey);
        if (value != null && !value.isEmpty()) return value;
        
        // Then try properties file
        value = props.getProperty(propKey);
        if (value != null && !value.isEmpty()) return value;
        
        // Finally try environment variable
        value = System.getenv(envKey);
        return (value != null && !value.isEmpty()) ? value : null;
    }
    
    private static File getJarDir() {
        try {
            String url = Conn.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File f = new File(url);
            return f.isFile() ? f.getParentFile() : f;
        } catch (Exception e) {
            return null;
        }
    }
    
    private static void showErrorDialog(String message) {
        try {
            javax.swing.JOptionPane.showMessageDialog(null, message, 
                "Database Connection Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (Exception ignored) {
            // If Swing is not available, just print to console
        }
    }
    
    // Add method to check if connection is valid
    public boolean isConnected() {
        try {
            return initialized && c != null && !c.isClosed() && c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    
    // Add method to close connection properly
    @Override
    public void close() {
        try {
            if (s != null) {
                s.close();
            }
            if (c != null && !c.isClosed()) {
                c.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}






/*
package university.management.system;

import java.sql.*;
import javax.swing.JOptionPane;

public class Conn {
    public Connection c;
    public Statement s;
    
    public Conn() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection("jdbc:mysql://localhost:3306/universitymanagementsystem", "root", "1716504726");
            s = c.createStatement();
        } catch (Exception e) {
            System.out.println("Database connection error: " + e);
            e.printStackTrace();
            // Show error message to user
            JOptionPane.showMessageDialog(null, "Database connection failed!\n\nError: " + e.getMessage(), 
                                         "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Add a method to check if connection is valid before using it
    public boolean isConnected() {
        try {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}


*/


/*

package university.management.system;

import java.sql.*;
import javax.swing.JOptionPane;
import java.util.Vector;

public class Conn {
    private Connection c;
    private Statement s;
    
    // Connection pool variables
    private static final int INITIAL_CONNECTIONS = 5;
    private static final int MAX_CONNECTIONS = 20;
    private static Vector<Connection> availableConnections = new Vector<>();
    private static Vector<Connection> usedConnections = new Vector<>();
    
    private static String url = "jdbc:mysql://localhost:3306/universitymanagementsystem";
    private static String username = "root";
    private static String password = "1716504726";
    
    // Static block to initialize connection pool
    static {
        initializeConnectionPool();
    }
    
    private static void initializeConnectionPool() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            for (int i = 0; i < INITIAL_CONNECTIONS; i++) {
                availableConnections.addElement(createNewConnection());
            }
        } catch (Exception e) {
            System.out.println("Connection pool initialization error: " + e);
            e.printStackTrace();
            showErrorDialog("Connection pool initialization failed!\n\nError: " + e.getMessage());
        }
    }
    
    private static Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    
    private static synchronized Connection getConnectionFromPool() throws SQLException {
        if (availableConnections.size() == 0) {
            if (usedConnections.size() < MAX_CONNECTIONS) {
                availableConnections.addElement(createNewConnection());
            } else {
                throw new SQLException("Maximum pool size reached, no available connections!");
            }
        }
        
        Connection connection = availableConnections.lastElement();
        availableConnections.removeElement(connection);
        usedConnections.addElement(connection);
        return connection;
    }
    
    private static synchronized void returnConnectionToPool(Connection connection) {
        if (connection != null) {
            usedConnections.removeElement(connection);
            availableConnections.addElement(connection);
        }
    }
    
    public Conn() {
        try {
            c = getConnectionFromPool();
            s = c.createStatement();
        } catch (Exception e) {
            System.out.println("Database connection error: " + e);
            e.printStackTrace();
            showErrorDialog("Database connection failed!\n\nError: " + e.getMessage());
        }
    }
    
    public Statement getStatement() {
        return s;
    }
    
    public Connection getConnection() {
        return c;
    }
    
    // Add a method to check if connection is valid before using it
    public boolean isConnected() {
        try {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    // Close method to return connection to pool
    public void close() {
        try {
            if (s != null) {
                s.close();
            }
            returnConnectionToPool(c);
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e);
            e.printStackTrace();
        }
    }
    
    // Method to close all connections (call this when application exits)
    public static void closeAllConnections() {
        closeConnections(availableConnections);
        closeConnections(usedConnections);
    }
    
    private static void closeConnections(Vector<Connection> connections) {
        for (Connection conn : connections) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e);
                e.printStackTrace();
            }
        }
        connections.clear();
    }
    
    private static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, 
                                     "Connection Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Add a shutdown hook to close all connections when application exits
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeAllConnections();
            System.out.println("All database connections closed.");
        }));
    }
}

*/