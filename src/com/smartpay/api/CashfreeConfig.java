package com.smartpay.api;

/**
 * Secure configuration for Cashfree Payment Gateway.
 * In production, these should be loaded from environment variables or a secure vault.
 */
public class CashfreeConfig {
    
    // Environment variables are preferred for security
    public static final String APP_ID = System.getenv("CASHFREE_APP_ID") != null ? 
                                       System.getenv("CASHFREE_APP_ID") : "TEST123456789";
                                       
    public static final String SECRET_KEY = System.getenv("CASHFREE_SECRET_KEY") != null ? 
                                           System.getenv("CASHFREE_SECRET_KEY") : "TEST_SECRET_KEY_PROD";
                                           
    public static final String BASE_URL = "https://sandbox.cashfree.com/pg"; // Use production URL for live
    
    public static final String API_VERSION = "2023-08-01";
}
