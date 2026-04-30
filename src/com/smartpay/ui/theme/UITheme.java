package com.smartpay.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * UITheme provides a centralized design system for the SmartPay application.
 * It defines the color palette, typography, and styling utilities.
 */
public class UITheme {
    // Color Palette
    public static final Color BG_MAIN = new Color(248, 249, 250);     // Very Light Gray (F8F9FA)
    public static final Color BG_CARD = new Color(255, 255, 255);     // Pure White
    public static final Color ACCENT_PRIMARY = new Color(10, 88, 202); // Modern Blue (0A58CA)
    public static final Color ACCENT_HOVER = new Color(13, 110, 253);  // Modern Blue Lighter
    public static final Color SUCCESS_GREEN = new Color(25, 135, 84);  // Bootstrap Green
    public static final Color ERROR_RED = new Color(220, 53, 69);      // Bootstrap Red
    public static final Color TEXT_PRIMARY = new Color(33, 37, 41);    // Dark Slate
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125); // Medium Gray
    public static final Color BORDER_COLOR = new Color(222, 226, 230);   // Light Gray Border

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    /**
     * Styles a panel as a "Card" with a specific background and a subtle border.
     */
    public static void styleCard(JPanel panel) {
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(25, 25, 25, 25)
        ));
    }

    /**
     * Styles a button with the SmartPay primary color and bold white text.
     */
    public static void styleButton(JButton button) {
        button.setBackground(ACCENT_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(FONT_NORMAL.deriveFont(Font.BOLD));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Simple Hover Effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) button.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) button.setBackground(ACCENT_PRIMARY);
            }
        });
    }

    /**
     * Styles a text field for a modern look.
     */
    public static void styleTextField(JTextField textField, String title) {
        textField.setBackground(BG_CARD);
        textField.setForeground(TEXT_PRIMARY);
        textField.setCaretColor(TEXT_PRIMARY);
        textField.setFont(FONT_NORMAL);
        
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                title, 0, 0, FONT_SMALL, TEXT_SECONDARY
            ),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(ACCENT_PRIMARY, 2, true),
                        title, 0, 0, FONT_SMALL, ACCENT_PRIMARY
                    ),
                    new EmptyBorder(4, 4, 4, 4)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        title, 0, 0, FONT_SMALL, TEXT_SECONDARY
                    ),
                    new EmptyBorder(5, 5, 5, 5)
                ));
            }
        });
    }

    /**
     * Creates a specialized JLabel for section headers.
     */
    public static JLabel createHeader(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_PRIMARY);
        label.setFont(FONT_HEADER);
        return label;
    }
}
