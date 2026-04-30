package com.smartpay.ui;

import com.smartpay.db.DBConnection;
import com.smartpay.sdk.PalmScanner;
import com.smartpay.security.CryptoProvider;
import com.smartpay.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class RegistrationApp extends JFrame {
    private static final Logger logger = Logger.getLogger(RegistrationApp.class.getName());
    private JTextField nameField, phoneField;
    private DefaultListModel<String> upiListModel;
    private JList<String> upiList;
    private String defaultUpi = null;
    private JButton scanButton, saveButton;
    private JLabel statusLabel;
    private byte[] capturedTemplate;
    private PalmScanner scanner;

    public RegistrationApp() {
        // Window Setup
        setTitle("SmartPay - Secure Enrollment");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);

        // Initialize Biometric Scanner
        scanner = new PalmScanner();

        // Main Layout
        setLayout(new BorderLayout());
        
        JLabel header = new JLabel("USER ENROLLMENT", SwingConstants.CENTER);
        header.setFont(UITheme.FONT_HEADER);
        header.setForeground(UITheme.ACCENT_PRIMARY);
        header.setBorder(new EmptyBorder(30, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        UITheme.styleCard(card);

        // Name & Phone
        nameField = new JTextField();
        UITheme.styleTextField(nameField, "Full Name");
        card.add(nameField);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        phoneField = new JTextField();
        UITheme.styleTextField(phoneField, "Phone Number");
        card.add(phoneField);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // UPI Management
        JLabel upiLabel = UITheme.createHeader("Payment Handles");
        upiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(upiLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        upiListModel = new DefaultListModel<>();
        upiList = new JList<>(upiListModel);
        upiList.setBackground(UITheme.BG_MAIN);
        upiList.setForeground(UITheme.TEXT_PRIMARY);
        JScrollPane upiScroll = new JScrollPane(upiList);
        upiScroll.setPreferredSize(new Dimension(400, 80));
        card.add(upiScroll);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel upiButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        upiButtons.setOpaque(false);
        JButton addUpiBtn = new JButton("Add Handle");
        UITheme.styleButton(addUpiBtn);
        upiButtons.add(addUpiBtn);
        card.add(upiButtons);
        card.add(Box.createRigidArea(new Dimension(0, 30)));

        // Biometrics
        scanButton = new JButton("CAPTURE PALM BIOMETRICS");
        UITheme.styleButton(scanButton);
        scanButton.setBackground(new Color(142, 68, 173)); // Purple for scan
        scanButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(scanButton);
        card.add(Box.createRigidArea(new Dimension(0, 30)));

        saveButton = new JButton("SAVE ENROLLMENT");
        UITheme.styleButton(saveButton);
        saveButton.setBackground(UITheme.SUCCESS_GREEN);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(saveButton);

        statusLabel = new JLabel("Enrollment Status: Ready", SwingConstants.CENTER);
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(statusLabel);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // Event: Add UPI
        addUpiBtn.addActionListener(e -> {
            String upi = JOptionPane.showInputDialog(this, "Enter UPI ID:");
            if (upi != null && !upi.trim().isEmpty()) {
                upiListModel.addElement(upi.trim());
                if (defaultUpi == null) defaultUpi = upi.trim();
            }
        });

        // Event: Scan Palm
        scanButton.addActionListener(e -> {
            if (!scanner.isConnected()) {
                showError("Scanner Not Connected");
                return;
            }
            capturedTemplate = scanner.captureTemplate();
            statusLabel.setText("Biometrics Captured Successfully");
            statusLabel.setForeground(UITheme.SUCCESS_GREEN);
        });

        // Event: Save to DB
        saveButton.addActionListener(e -> saveUserToDatabase());
    }

    private void saveUserToDatabase() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || upiListModel.isEmpty() || capturedTemplate == null) {
            showError("Missing Required Fields or Biometrics");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String userSql = "INSERT INTO users (name, phone) VALUES (?, ?)";
                PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, name);
                userStmt.setString(2, phone);
                userStmt.executeUpdate();

                ResultSet rs = userStmt.getGeneratedKeys();
                int userId = rs.next() ? rs.getInt(1) : -1;

                byte[] encryptedTemplate = CryptoProvider.encrypt(capturedTemplate);
                String palmSql = "INSERT INTO palm_templates (user_id, template_data) VALUES (?, ?)";
                PreparedStatement palmStmt = conn.prepareStatement(palmSql);
                palmStmt.setInt(1, userId);
                palmStmt.setBytes(2, encryptedTemplate); 
                palmStmt.executeUpdate();

                String paymentSql = "INSERT INTO payment_methods (user_id, upi_id, is_default) VALUES (?, ?, ?)";
                PreparedStatement paymentStmt = conn.prepareStatement(paymentSql);
                for (int i = 0; i < upiListModel.getSize(); i++) {
                    String upi = upiListModel.getElementAt(i);
                    paymentStmt.setInt(1, userId);
                    paymentStmt.setString(2, upi);
                    paymentStmt.setBoolean(3, upi.equals(defaultUpi));
                    paymentStmt.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Enrollment Complete. User ID: " + userId);
                resetForm();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            logger.severe("User enrollment failed: " + ex.getMessage());
            if (!com.smartpay.db.DBConnection.checkConnection()) {
                JOptionPane.showMessageDialog(this, 
                    "PERSISTENCE ERROR: Database is OFFLINE.\n\n" +
                    "You are currently in 'Emergency Bypass' mode. \n" +
                    "Registration data cannot be saved until you start your MySQL server.", 
                    "Offline Mode", JOptionPane.WARNING_MESSAGE);
            } else {
                showError("System Error: " + ex.getMessage());
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Enrollment Error", JOptionPane.ERROR_MESSAGE);
    }

    private void resetForm() {
        nameField.setText("");
        phoneField.setText("");
        upiListModel.clear();
        defaultUpi = null;
        capturedTemplate = null;
        statusLabel.setText("Enrollment Status: Ready");
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationApp().setVisible(true));
    }
}
