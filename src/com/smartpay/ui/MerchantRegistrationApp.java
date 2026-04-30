package com.smartpay.ui;

import com.smartpay.db.DBConnection;
import com.smartpay.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

/**
 * MerchantRegistrationApp handles the onboarding of new merchants into the SmartPay system.
 */
public class MerchantRegistrationApp extends JFrame {
    private static final Logger logger = Logger.getLogger(MerchantRegistrationApp.class.getName());
    
    private JTextField nameField, upiField, deviceField, locationField;
    private JButton saveButton;

    public MerchantRegistrationApp() {
        // Window Setup
        setTitle("SmartPay - Merchant Onboarding");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);

        // Main Layout
        setLayout(new BorderLayout());
        
        JLabel header = new JLabel("MERCHANT ONBOARDING", SwingConstants.CENTER);
        header.setFont(UITheme.FONT_HEADER);
        header.setForeground(UITheme.ACCENT_PRIMARY);
        header.setBorder(new EmptyBorder(30, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        UITheme.styleCard(card);

        // Fields
        nameField = new JTextField();
        UITheme.styleTextField(nameField, "Business Name");
        card.add(nameField);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        upiField = new JTextField();
        UITheme.styleTextField(upiField, "Merchant UPI ID");
        card.add(upiField);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        deviceField = new JTextField();
        UITheme.styleTextField(deviceField, "Scanner Device ID");
        card.add(deviceField);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        locationField = new JTextField();
        UITheme.styleTextField(locationField, "Store Location (City/Mall)");
        card.add(locationField);
        card.add(Box.createRigidArea(new Dimension(0, 30)));

        saveButton = new JButton("COMPLETE ONBOARDING");
        UITheme.styleButton(saveButton);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(saveButton);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // Event: Save Merchant
        saveButton.addActionListener(e -> saveMerchant());
    }

    private void saveMerchant() {
        String name = nameField.getText().trim();
        String upi = upiField.getText().trim();
        String devId = deviceField.getText().trim();
        String loc = locationField.getText().trim();

        if (name.isEmpty() || upi.isEmpty() || devId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields (Name, UPI, Device ID).", "Onboarding Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO merchants (merchant_name, upi_id, device_id, location) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, upi);
            stmt.setString(3, devId);
            stmt.setString(4, loc);

            stmt.executeUpdate();
            
            logger.info("Merchant registered successfully: " + name);
            JOptionPane.showMessageDialog(this, "Merchant \"" + name + "\" onboarded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            this.dispose();

        } catch (Exception ex) {
            logger.severe("Merchant registration failed: " + ex.getMessage());
            if (!com.smartpay.db.DBConnection.checkConnection()) {
                JOptionPane.showMessageDialog(this, 
                    "PERSISTENCE ERROR: Database is OFFLINE.\n\n" +
                    "You are currently in 'Emergency Bypass' mode. \n" +
                    "Data cannot be saved until you start your MySQL server.", 
                    "Offline Mode", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "System Error: Unable to save merchant details.\nError: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MerchantRegistrationApp().setVisible(true));
    }
}
