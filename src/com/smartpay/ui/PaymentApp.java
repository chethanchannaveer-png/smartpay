package com.smartpay.ui;

import com.smartpay.api.AIClient;
import com.smartpay.api.CashfreeService;
import com.smartpay.db.DBConnection;
import com.smartpay.sdk.PalmScanner;
import com.smartpay.security.CryptoProvider;
import com.smartpay.ui.theme.UITheme;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class PaymentApp extends JFrame {
    private static final Logger logger = Logger.getLogger(PaymentApp.class.getName());
    private JTextField amountField;
    private JLabel statusLabel, userNameLabel, upiLabel;
    private JButton scanButton;
    
    private PalmScanner scanner;
    private AIClient aiClient;
    private CashfreeService cashfreeService;

    public PaymentApp() {
        // Initialize Core Components
        scanner = new PalmScanner();
        aiClient = new AIClient();
        cashfreeService = new CashfreeService();

        // Window Setup
        setTitle("SmartPay - Secure Payment Terminal");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);

        // Main Layout
        setLayout(new BorderLayout());
        
        // Header
        JLabel header = new JLabel("SECURE CHECKOUT", SwingConstants.CENTER);
        header.setFont(UITheme.FONT_HEADER);
        header.setForeground(UITheme.ACCENT_PRIMARY);
        header.setBorder(new EmptyBorder(30, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // Center Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        UITheme.styleCard(card);
        card.setMaximumSize(new Dimension(400, 400));

        // Amount Input
        amountField = new JTextField();
        UITheme.styleTextField(amountField, "Amount (INR)");
        amountField.setFont(UITheme.FONT_TITLE);
        amountField.setHorizontalAlignment(JTextField.CENTER);
        card.add(amountField);
        card.add(Box.createRigidArea(new Dimension(0, 30)));

        // Scan Button
        scanButton = new JButton("AUTHENTICATE & PAY");
        UITheme.styleButton(scanButton);
        scanButton.setPreferredSize(new Dimension(300, 60));
        scanButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(scanButton);
        card.add(Box.createRigidArea(new Dimension(0, 30)));

        // Status Area
        statusLabel = new JLabel("Ready to Scan", SwingConstants.CENTER);
        statusLabel.setFont(UITheme.FONT_NORMAL);
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        userNameLabel = new JLabel("---", SwingConstants.CENTER);
        userNameLabel.setFont(UITheme.FONT_HEADER);
        userNameLabel.setForeground(UITheme.TEXT_PRIMARY);
        userNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(userNameLabel);

        upiLabel = new JLabel("---", SwingConstants.CENTER);
        upiLabel.setFont(UITheme.FONT_SMALL);
        upiLabel.setForeground(UITheme.TEXT_SECONDARY);
        upiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(upiLabel);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // Event Listener
        scanButton.addActionListener(e -> processPayment());
    }

    private void processPayment() {
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showError("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showError("Invalid amount format.");
            return;
        }

        new Thread(() -> {
            try {
                updateStatus("Scanning Palm...", UITheme.ACCENT_PRIMARY);
                
                if (!scanner.isConnected()) {
                    throw new Exception("Scanner Hardware Disconnected");
                }
                
                byte[] template = scanner.captureTemplate();
                updateStatus("Securing Biometrics...", UITheme.ACCENT_PRIMARY);
                
                byte[] encrypted = CryptoProvider.encrypt(template);
                String base64Encrypted = CryptoProvider.toBase64(encrypted);

                updateStatus("Authenticating User...", UITheme.ACCENT_PRIMARY);
                String aiResult = aiClient.sendTemplateForMatching(base64Encrypted);
                JSONObject json = new JSONObject(aiResult);
                
                if (json.has("user_id")) {
                    int userId = json.getInt("user_id");
                    UserSession session = fetchUserDetails(userId);
                    
                    if (session != null) {
                        if (!validateLimits(userId, amount, session)) return;

                        SwingUtilities.invokeLater(() -> {
                            userNameLabel.setText(session.name);
                            upiLabel.setText("UPI: " + session.upiId);
                            updateStatus("PAYMENT SUCCESSFUL", UITheme.SUCCESS_GREEN);
                        });
                        
                        recordTransaction(userId, 1, amount, "completed", "Auth Success", "TXN-" + System.currentTimeMillis());
                        Thread.sleep(2000);
                        resetScreen();
                    }
                } else {
                    updateStatus("IDENTITY REJECTED", UITheme.ERROR_RED);
                    logAuthentication(null, "failure");
                    showError("Palm not recognized. Please register first.");
                }

            } catch (Exception ex) {
                updateStatus("SYSTEM ERROR", UITheme.ERROR_RED);
                showError(ex.getMessage());
            }
        }).start();
    }

    private void updateStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
            statusLabel.setForeground(color);
        });
    }

    private void showError(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Payment Error", JOptionPane.ERROR_MESSAGE));
    }

    private UserSession fetchUserDetails(int userId) {
        String query = "SELECT u.name, p.upi_id, u.max_per_transaction, u.max_daily_limit FROM users u " +
                       "JOIN payment_methods p ON u.user_id = p.user_id " +
                       "WHERE u.user_id = ? AND p.is_default = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserSession(rs.getString("name"), rs.getString("upi_id"),
                        rs.getDouble("max_per_transaction"), rs.getDouble("max_daily_limit"));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }

    private boolean validateLimits(int userId, double amount, UserSession session) {
        if (amount > session.maxPerTransaction) {
            showError("Transaction limit exceeded: ₹" + session.maxPerTransaction);
            return false;
        }
        return true;
    }

    private void recordTransaction(int userId, Integer merchantId, double amount, String status, String gatewayResponse, String txRef) throws Exception {
        String sql = "INSERT INTO transactions (user_id, merchant_id, amount, status, gateway_response, transaction_reference) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            if (merchantId != null) stmt.setInt(2, merchantId); else stmt.setNull(2, java.sql.Types.INTEGER);
            stmt.setDouble(3, amount);
            stmt.setString(4, status);
            stmt.setString(5, gatewayResponse);
            stmt.setString(6, txRef);
            stmt.executeUpdate();
        }
    }

    private void logAuthentication(Integer userId, String result) {
        String sql = "INSERT INTO auth_logs (user_id, device_id, result) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId != null) stmt.setInt(1, userId); else stmt.setNull(1, java.sql.Types.INTEGER);
            stmt.setString(2, "SCANNER_001");
            stmt.setString(3, result);
            stmt.executeUpdate();
        } catch (Exception ex) { logger.severe("Log failed: " + ex.getMessage()); }
    }

    private void resetScreen() {
        SwingUtilities.invokeLater(() -> {
            amountField.setText("");
            statusLabel.setText("Ready to Scan");
            statusLabel.setForeground(UITheme.TEXT_SECONDARY);
            userNameLabel.setText("---");
            upiLabel.setText("---");
        });
    }

    private static class UserSession {
        String name, upiId;
        double maxPerTransaction, maxDailyLimit;
        UserSession(String n, String u, double pt, double dl) {
            this.name = n; this.upiId = u; this.maxPerTransaction = pt; this.maxDailyLimit = dl;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PaymentApp().setVisible(true));
    }
}
