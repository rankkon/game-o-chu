package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection instance;
    private final String url = "jdbc:mysql://localhost:3306/ochu";
    private final String user = "root";
    private final String password = "";

    private DBConnection() {
        System.out.println("[DEBUG] Initializing DBConnection...");
        System.out.println("[DEBUG] URL: " + url);
        System.out.println("[DEBUG] User: " + user);
        System.out.println("[DEBUG] Loading MySQL Driver...");

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DEBUG] MySQL Driver loaded successfully.");

            // Test connection
            System.out.println("[DEBUG] Attempting to connect to database...");
            try (Connection testConn = DriverManager.getConnection(url, user, password)) {
                if (testConn != null && !testConn.isClosed()) {
                    System.out.println("[SUCCESS] Database connection successful!");
                } else {
                    System.out.println("[WARNING] Connection object is null or closed.");
                }
            }

        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL JDBC Driver not found.");
            e.printStackTrace();

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to connect to database.");
            System.err.println("[DEBUG] SQLException Message: " + e.getMessage());
            System.err.println("[DEBUG] SQLState: " + e.getSQLState());
            System.err.println("[DEBUG] Vendor Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            System.out.println("[DEBUG] Creating new DBConnection instance...");
            instance = new DBConnection();
        } else {
            System.out.println("[DEBUG] Using existing DBConnection instance...");
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        System.out.println("[DEBUG] Opening new database connection...");
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("[DEBUG] Connection established successfully!");
            return conn;
        } catch (SQLException e) {
            System.err.println("[ERROR] Unable to establish connection in getConnection().");
            System.err.println("[DEBUG] SQLException Message: " + e.getMessage());
            System.err.println("[DEBUG] SQLState: " + e.getSQLState());
            System.err.println("[DEBUG] Vendor Error Code: " + e.getErrorCode());
            throw e;
        }
    }
}
