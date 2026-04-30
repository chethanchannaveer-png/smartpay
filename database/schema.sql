-- SmartPay Database Schema
-- Created: 2026-03-12
-- Target: MySQL

CREATE DATABASE IF NOT EXISTS smartpaydb;
USE smartpaydb;

-- 1. Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role ENUM('USER', 'ADMIN', 'MERCHANT') DEFAULT 'USER'
);

-- 2. Palm Templates table
CREATE TABLE IF NOT EXISTS palm_templates (
    template_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    template_data LONGBLOB NOT NULL,
    device_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Payment Methods table
CREATE TABLE IF NOT EXISTS payment_methods (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    upi_id VARCHAR(100) NOT NULL,
    bank_name VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. Merchants table
CREATE TABLE IF NOT EXISTS merchants (
    merchant_id INT AUTO_INCREMENT PRIMARY KEY,
    merchant_name VARCHAR(100) NOT NULL,
    upi_id VARCHAR(100) NOT NULL,
    device_id VARCHAR(50),
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    merchant_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status ENUM('SUCCESS', 'FAILED', 'PENDING') DEFAULT 'PENDING',
    gateway_response TEXT,
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (merchant_id) REFERENCES merchants(merchant_id) ON DELETE CASCADE
);

-- 6. Authentication Logs table
CREATE TABLE IF NOT EXISTS authentication_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    device_id VARCHAR(50),
    result ENUM('SUCCESS', 'FAILED') NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 7. Admins table
CREATE TABLE IF NOT EXISTS admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Default system data
INSERT IGNORE INTO admins (username, password) VALUES ('admin', 'password123');
INSERT IGNORE INTO users (name, phone, role) VALUES ('System Admin', '0000000000', 'ADMIN');
