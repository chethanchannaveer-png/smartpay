package com.smartpay.ui;

import com.smartpay.db.DBConnection;
import com.smartpay.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * LoginApp is the entry screen for the SmartPay system.
 * It validates admin credentials against the MySQL database.
 */
public class LoginApp extends JFrame {
    private static final Logger logger = Logger.getLogger(LoginApp.class.getName());

    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;

    public LoginApp() {
        setTitle("SmartPay - Admin Login");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);

        // Core Layout
        setLayout(new GridBagLayout());
        
        // Form Card
        JPanel card = new JPanel(new GridLayout(4, 1, 15, 15));
        UITheme.styleCard(card);
        card.setPreferredSize(new Dimension(350, 300));

        // Header
        JLabel header = new JLabel("SmartPay", SwingConstants.CENTER);
        header.setFont(UITheme.FONT_TITLE);
        header.setForeground(UITheme.ACCENT_PRIMARY);
        card.add(header);

        // Input Fields
        userField = new JTextField();
        UITheme.styleTextField(userField, "Username");
        card.add(userField);

        passField = new JPasswordField();
        passField.setBackground(UITheme.BG_CARD);
        passField.setForeground(UITheme.TEXT_PRIMARY);
        passField.setCaretColor(UITheme.TEXT_PRIMARY);
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                "Password",
                0, 0, UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY
            ),
            new javax.swing.border.EmptyBorder(5, 5, 5, 5)
        ));
        
        passField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(UITheme.ACCENT_PRIMARY, 2, true),
                        "Password", 0, 0, UITheme.FONT_SMALL, UITheme.ACCENT_PRIMARY
                    ),
                    new javax.swing.border.EmptyBorder(4, 4, 4, 4)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                passField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                        "Password", 0, 0, UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY
                    ),
                    new javax.swing.border.EmptyBorder(5, 5, 5, 5)
                ));
            }
        });
        card.add(passField);

        // Action Button
        loginButton = new JButton("LOGIN TO TERMINAL");
        UITheme.styleButton(loginButton);
        card.add(loginButton);

        add(card);

        // Event: Login
        loginButton.addActionListener(e -> performLogin());
        
        // Enter key listener
        passField.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both credentials.", "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                logger.info("Login successful for user: " + username + " (Role: " + role + ")");
                
                // Close login and open dashboard with role info
                new SmartPayDashboard(username, role).setVisible(true);
                this.dispose();
            } else {
                logger.warning("Failed login attempt for user: " + username);
                JOptionPane.showMessageDialog(this, "Invalid credentials entered.\nUsername: " + username + "\nPassword: " + password, "Access Denied", JOptionPane.ERROR_MESSAGE);
                passField.setText("");
            }

        } catch (Exception ex) {
            logger.severe("Database connection error: " + ex.getMessage());
            
            // EMERGENCY BYPASS: Allow admin/password123 if DB is offline
            if ("admin".equals(username) && "password123".equals(password)) {
                logger.warning("DATABASE OFFLINE - Using Emergency Bypass for admin login.");
                new SmartPayDashboard("Admin (Simulated)", "admin").setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Connection Error: Database is offline.\n(To bypass, use admin / password123)", "System Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginApp().setVisible(true));
    }
}
