package com.smartpay.ui;

import com.smartpay.ui.theme.UITheme;
import com.smartpay.sdk.PalmScanner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SmartPayDashboard extends JFrame {
    private String userRole;
    private JLabel scannerStatusLabel;
    private PalmScanner dummyScanner;

    public SmartPayDashboard(String username, String role) {
        this.userRole = role;
        this.dummyScanner = new PalmScanner();
        
        setTitle("SmartPay Terminal - " + username);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);

        // Sidebar / Info Panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_CARD);
        sidebar.setPreferredSize(new Dimension(200, 500));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel logo = new JLabel("SmartPay");
        logo.setFont(UITheme.FONT_HEADER);
        logo.setForeground(UITheme.ACCENT_PRIMARY);
        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        JLabel userLabel = new JLabel("User: " + username);
        userLabel.setForeground(UITheme.TEXT_PRIMARY);
        userLabel.setFont(UITheme.FONT_NORMAL);
        sidebar.add(userLabel);
        JLabel roleLabel = new JLabel("Role: " + role.toUpperCase());
        roleLabel.setForeground(UITheme.TEXT_SECONDARY);
        roleLabel.setFont(UITheme.FONT_SMALL);
        sidebar.add(roleLabel);
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));
        JButton userMgmtBtn = new JButton("User Management");
        userMgmtBtn.setFont(UITheme.FONT_SMALL);
        userMgmtBtn.setBackground(UITheme.BG_CARD);
        userMgmtBtn.setForeground(UITheme.ACCENT_PRIMARY);
        userMgmtBtn.setFocusPainted(false);
        userMgmtBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userMgmtBtn.addActionListener(e -> new UserManagement().setVisible(true));
        sidebar.add(userMgmtBtn);

        // Main Grid Panel
        JPanel mainGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        mainGrid.setBackground(UITheme.BG_MAIN);
        mainGrid.setBorder(new EmptyBorder(30, 30, 30, 30));

        JButton regButton = createModuleButton("User Registration", "Add new palm secure users");
        JButton merchantButton = createModuleButton("Merchants", "Onboard retail partners");
        JButton historyButton = createModuleButton("Analytics", "View transaction history");
        JButton payButton = createModuleButton("Payment Terminal", "Launch payment interface");

        mainGrid.add(regButton);
        mainGrid.add(merchantButton);
        mainGrid.add(historyButton);
        mainGrid.add(payButton);

        // Scanner Status (Bottom Bar)
        scannerStatusLabel = new JLabel("● CHECKING SCANNER STATUS...", SwingConstants.RIGHT);
        scannerStatusLabel.setFont(UITheme.FONT_SMALL);
        scannerStatusLabel.setBorder(new EmptyBorder(10, 10, 10, 20));
        scannerStatusLabel.setForeground(UITheme.TEXT_SECONDARY);

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(mainGrid, BorderLayout.CENTER);
        add(scannerStatusLabel, BorderLayout.SOUTH);

        // RBAC: Restrict Actions for Merchants
        if ("merchant".equalsIgnoreCase(userRole)) {
            regButton.setEnabled(false);
            merchantButton.setEnabled(false);
            historyButton.setEnabled(false);
        }

        // Listeners
        regButton.addActionListener(e -> new RegistrationApp().setVisible(true));
        merchantButton.addActionListener(e -> new MerchantRegistrationApp().setVisible(true));
        historyButton.addActionListener(e -> new TransactionHistoryApp().setVisible(true));
        payButton.addActionListener(e -> new PaymentApp().setVisible(true));

        // Initialize Status Monitor Timer
        Timer statusTimer = new Timer(3000, e -> updateScannerStatus());
        statusTimer.start();
        updateScannerStatus();
    }

    private void updateScannerStatus() {
        boolean dbOnline = com.smartpay.db.DBConnection.checkConnection();
        boolean scannerOnline = dummyScanner.isConnected();
        boolean isSimulating = "true".equalsIgnoreCase(System.getProperty("simulate.scanner", "false"));

        StringBuilder sb = new StringBuilder("<html><div style='text-align: right;'>");
        
        // Database Status
        if (dbOnline) {
            sb.append("<span style='color: #198754;'>● DATABASE: ONLINE</span>");
        } else {
            sb.append("<span style='color: #DC3545;'>○ DATABASE: OFFLINE (SIMULATED MODE)</span>");
        }
        
        sb.append(" &nbsp;&nbsp; | &nbsp;&nbsp; ");

        // Scanner Status
        if (scannerOnline) {
            String label = isSimulating ? "● SCANNER: SIMULATED" : "● SCANNER: ONLINE";
            sb.append("<span style='color: #0A58CA;'>").append(label).append("</span>");
        } else {
            sb.append("<span style='color: #6C757D;'>○ SCANNER: OFFLINE</span>");
        }

        sb.append("</div></html>");
        scannerStatusLabel.setText(sb.toString());
    }

    private JButton createModuleButton(String title, String desc) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBackground(UITheme.BG_CARD);
        btn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);
        titleLbl.setFont(UITheme.FONT_NORMAL.deriveFont(Font.BOLD));
        
        JLabel descLbl = new JLabel("<html><center>" + desc + "</center></html>", SwingConstants.CENTER);
        descLbl.setForeground(UITheme.TEXT_SECONDARY);
        descLbl.setFont(UITheme.FONT_SMALL);

        JPanel content = new JPanel(new GridLayout(2, 1, 5, 5));
        content.setOpaque(false);
        content.add(titleLbl);
        content.add(descLbl);
        
        btn.add(content, BorderLayout.CENTER);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(248, 249, 250)); // Slightly off-white hover
                btn.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_PRIMARY, 1));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(UITheme.BG_CARD);
                btn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
            }
        });

        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartPayDashboard("Guest", "admin").setVisible(true));
    }
}
