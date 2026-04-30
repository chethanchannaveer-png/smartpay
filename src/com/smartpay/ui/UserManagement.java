package com.smartpay.ui;

import com.smartpay.db.DBConnection;
import com.smartpay.ui.theme.UITheme;
import com.smartpay.sdk.PalmScanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class UserManagement extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private PalmScanner palmScanner;

    public UserManagement() {
        setTitle("SmartPay - User Management & Biometrics");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout(10, 10));

        this.palmScanner = new PalmScanner();
        initUI();
        loadUserData();
    }

    private void initUI() {
        // --- Header Section ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(UITheme.BG_MAIN);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        
        JLabel lblTitle = new JLabel("USER MANAGEMENT (BIOMETRIC)");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.ACCENT_PRIMARY);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // --- Table Section ---
        String[] columnNames = {"#", "ID", "Name", "Phone", "Role", "Default UPI ID", "Biometric", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setBackground(UITheme.BG_CARD);
        userTable.setForeground(UITheme.TEXT_PRIMARY);
        userTable.setGridColor(UITheme.BORDER_COLOR);
        userTable.setFont(UITheme.FONT_NORMAL);
        userTable.setRowHeight(30);
        userTable.getTableHeader().setBackground(UITheme.BG_MAIN);
        userTable.getTableHeader().setForeground(UITheme.ACCENT_PRIMARY);
        userTable.getTableHeader().setFont(UITheme.FONT_HEADER);

        // Column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(40);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(UITheme.BG_MAIN);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UITheme.BG_MAIN);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- Button Section ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(UITheme.BG_MAIN);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        btnAdd = new JButton("ADD USER");
        btnEdit = new JButton("EDIT USER");
        btnDelete = new JButton("DELETE USER");
        btnRefresh = new JButton("REFRESH");

        UITheme.styleButton(btnAdd);
        UITheme.styleButton(btnEdit);
        UITheme.styleButton(btnDelete);
        UITheme.styleButton(btnRefresh);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnAdd.addActionListener(e -> showAddUserDialog());
        btnEdit.addActionListener(e -> editSelectedUser());
        btnDelete.addActionListener(e -> deleteSelectedUser());
        btnRefresh.addActionListener(e -> loadUserData());
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        String sql = "SELECT u.*, pm.upi_id, (SELECT count(*) FROM palm_templates pt WHERE pt.user_id = u.user_id) as bio_count " +
                     "FROM users u " +
                     "LEFT JOIN payment_methods pm ON u.user_id = pm.user_id AND pm.is_default = 1";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int rowNum = 1;
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rowNum++); // Serial Number
                row.add(rs.getInt("user_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("role"));
                row.add(rs.getString("upi_id") != null ? rs.getString("upi_id") : "N/A");
                row.add(rs.getInt("bio_count") > 0 ? "ENABLED" : "NONE");
                row.add(rs.getTimestamp("created_at"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showAddUserDialog() {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField upiField = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"USER", "ADMIN", "MERCHANT"});
        
        JButton btnScan = new JButton("SCAN PALM BIOMETRIC");
        UITheme.styleButton(btnScan);
        btnScan.setBackground(UITheme.BG_CARD);
        
        final byte[][] capturedTemplate = {null};
        btnScan.addActionListener(e -> {
            btnScan.setText("PLEASE PLACE PALM ON SCANNER...");
            btnScan.setEnabled(false);
            new Thread(() -> {
                byte[] template = palmScanner.captureTemplate();
                SwingUtilities.invokeLater(() -> {
                    if (template != null) {
                        capturedTemplate[0] = template;
                        btnScan.setText("PALM SCANNED SUCCESSFULLY ✅");
                        btnScan.setBackground(UITheme.SUCCESS_GREEN);
                    } else {
                        btnScan.setText("SCAN FAILED - RETRY?");
                        btnScan.setEnabled(true);
                    }
                });
            }).start();
        });

        UITheme.styleTextField(nameField, "Name");
        UITheme.styleTextField(phoneField, "Phone");
        UITheme.styleTextField(upiField, "UPI ID (Optional)");

        Object[] message = {
            "Step 1: Basic Info",
            "Name:", nameField,
            "Phone:", phoneField,
            "UPI ID:", upiField,
            "Role:", roleBox,
            "Step 2: Biometrics",
            btnScan
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New User & Palm Template", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String phone = phoneField.getText();
            String role = (String) roleBox.getSelectedItem();
            String upiId = upiField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || capturedTemplate[0] == null) {
                JOptionPane.showMessageDialog(this, "Name, Phone, and Palm Scan are required!");
                return;
            }

            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);

                // 1. Insert User
                PreparedStatement pstmtUser = conn.prepareStatement("INSERT INTO users (name, phone, role) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                pstmtUser.setString(1, name);
                pstmtUser.setString(2, phone);
                pstmtUser.setString(3, role);
                pstmtUser.executeUpdate();

                ResultSet rs = pstmtUser.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    
                    // 2. Insert UPI ID
                    if (!upiId.isEmpty()) {
                        PreparedStatement pstmtUPI = conn.prepareStatement("INSERT INTO payment_methods (user_id, upi_id, is_default) VALUES (?, ?, 1)");
                        pstmtUPI.setInt(1, userId);
                        pstmtUPI.setString(2, upiId);
                        pstmtUPI.executeUpdate();
                    }

                    // 3. Insert Palm Template
                    PreparedStatement pstmtBio = conn.prepareStatement("INSERT INTO palm_templates (user_id, template_data) VALUES (?, ?)");
                    pstmtBio.setInt(1, userId);
                    pstmtBio.setBytes(2, capturedTemplate[0]);
                    pstmtBio.executeUpdate();
                }

                conn.commit();
                loadUserData();
                JOptionPane.showMessageDialog(this, "User registered with Biometrics successfully!");
            } catch (SQLException e) {
                if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
                JOptionPane.showMessageDialog(this, "Error adding user: " + e.getMessage());
            } finally {
                if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
            }
        }
    }

    private void editSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to edit!");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 1);
        String currentName = (String) tableModel.getValueAt(selectedRow, 2);
        String currentPhone = (String) tableModel.getValueAt(selectedRow, 3);
        String currentRole = (String) tableModel.getValueAt(selectedRow, 4);
        String currentUPI = (String) tableModel.getValueAt(selectedRow, 5);
        if ("N/A".equals(currentUPI)) currentUPI = "";

        JTextField nameField = new JTextField(currentName);
        JTextField phoneField = new JTextField(currentPhone);
        JTextField upiField = new JTextField(currentUPI);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"USER", "ADMIN", "MERCHANT"});
        roleBox.setSelectedItem(currentRole);

        UITheme.styleTextField(nameField, "Name");
        UITheme.styleTextField(phoneField, "Phone");
        UITheme.styleTextField(upiField, "UPI ID");

        Object[] message = {
            "Edit User Details",
            "Name:", nameField,
            "Phone:", phoneField,
            "UPI ID:", upiField,
            "Role:", roleBox
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);

                PreparedStatement pstmtUser = conn.prepareStatement("UPDATE users SET name=?, phone=?, role=? WHERE user_id=?");
                pstmtUser.setString(1, nameField.getText());
                pstmtUser.setString(2, phoneField.getText());
                pstmtUser.setString(3, (String) roleBox.getSelectedItem());
                pstmtUser.setInt(4, userId);
                pstmtUser.executeUpdate();

                String newUPI = upiField.getText().trim();
                PreparedStatement pstmtCheck = conn.prepareStatement("SELECT payment_id FROM payment_methods WHERE user_id=? AND is_default=1");
                pstmtCheck.setInt(1, userId);
                ResultSet rs = pstmtCheck.executeQuery();
                
                if (rs.next()) {
                    if (newUPI.isEmpty()) {
                        PreparedStatement pDel = conn.prepareStatement("DELETE FROM payment_methods WHERE user_id=? AND is_default=1");
                        pDel.setInt(1, userId);
                        pDel.executeUpdate();
                    } else {
                        PreparedStatement pUpd = conn.prepareStatement("UPDATE payment_methods SET upi_id=? WHERE user_id=? AND is_default=1");
                        pUpd.setString(1, newUPI);
                        pUpd.setInt(2, userId);
                        pUpd.executeUpdate();
                    }
                } else if (!newUPI.isEmpty()) {
                    PreparedStatement pIns = conn.prepareStatement("INSERT INTO payment_methods (user_id, upi_id, is_default) VALUES (?, ?, 1)");
                    pIns.setInt(1, userId);
                    pIns.setString(2, newUPI);
                    pIns.executeUpdate();
                }

                conn.commit();
                loadUserData();
            } catch (SQLException e) {
                if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
                JOptionPane.showMessageDialog(this, "Error updating user: " + e.getMessage());
            } finally {
                if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
            }
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to delete!");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "PERMANENT DELETE user ID: " + userId + "?\nAll biometric templates and payment data will be removed.", "Confirm Permanent Deletion", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE user_id=?")) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
                loadUserData();
                JOptionPane.showMessageDialog(this, "User deleted permanently.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserManagement().setVisible(true));
    }
}
