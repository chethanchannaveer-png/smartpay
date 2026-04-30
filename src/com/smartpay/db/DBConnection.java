package com.smartpay.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database credentials
    private static final String URL = System.getenv("DB_URL") != null ? 
                                     System.getenv("DB_URL") : "jdbc:mysql://localhost:3306/smartpaydb";
    private static final String USER = System.getenv("DB_USER") != null ? 
                                      System.getenv("DB_USER") : "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? 
                                          System.getenv("DB_PASSWORD") : ""; 

    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Ensure it is in the classpath.", e);
        }
    }

    public static boolean checkConnection() {
        try (Connection conn = getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
