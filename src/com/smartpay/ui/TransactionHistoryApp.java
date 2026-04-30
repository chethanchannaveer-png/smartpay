package com.smartpay.ui;

import com.smartpay.db.DBConnection;
import com.smartpay.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * TransactionHistoryApp provides a tabular view of all payments in the system.
 */
public class TransactionHistoryApp extends JFrame {
    private static final Logger logger = Logger.getLogger(TransactionHistoryApp.class.getName());
    
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    public TransactionHistoryApp() {
        setTitle("SmartPay - Transaction History");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);

        // UI Components
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UITheme.BG_MAIN);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("All System Transactions", SwingConstants.LEFT);
        headerLabel.setFont(UITheme.FONT_TITLE.deriveFont(20f));
        headerLabel.setForeground(UITheme.ACCENT_PRIMARY);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Table Setup
        String[] columnNames = {"Tx ID", "User Name", "Merchant Name", "Amount (₹)", "Status", "Gateway Resp", "Tx Reference", "Date/Time"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // View only
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(25);
        transactionTable.getTableHeader().setFont(UITheme.FONT_NORMAL.deriveFont(Font.BOLD));
        transactionTable.getTableHeader().setBackground(UITheme.BG_MAIN);
        transactionTable.getTableHeader().setForeground(UITheme.TEXT_PRIMARY);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(UITheme.BG_MAIN);
        JButton refreshButton = new JButton("Refresh History");
        UITheme.styleButton(refreshButton);
        
        controlPanel.add(refreshButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Load Initial Data
        loadTransactionHistory();

        // Event: Refresh
        refreshButton.addActionListener(e -> loadTransactionHistory());
    }

    private void loadTransactionHistory() {
        tableModel.setRowCount(0); // Clear existing rows

        String query = "SELECT t.transaction_id, u.name as user_name, " +
                       "IFNULL(m.merchant_name, 'Direct Transfer') as merchant_name, " +
                       "t.amount, t.status, t.gateway_response, t.transaction_reference, t.transaction_time " +
                       "FROM transactions t " +
                       "JOIN users u ON t.user_id = u.user_id " +
                       "LEFT JOIN merchants m ON t.merchant_id = m.merchant_id " +
                       "ORDER BY t.transaction_time DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("transaction_id"),
                    rs.getString("user_name"),
                    rs.getString("merchant_name"),
                    String.format("%.2f", rs.getDouble("amount")),
                    rs.getString("status").toUpperCase(),
                    rs.getString("gateway_response"),
                    rs.getString("transaction_reference"),
                    rs.getTimestamp("transaction_time").toString()
                };
                tableModel.addRow(row);
            }
            logger.info("Transaction history reloaded.");

        } catch (Exception ex) {
            logger.severe("Failed to load transactions: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TransactionHistoryApp().setVisible(true));
    }
}
