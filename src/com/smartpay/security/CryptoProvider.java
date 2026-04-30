package com.smartpay.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * CryptoProvider handles AES encryption and decryption of sensitive biometric data.
 * It uses AES-128 in ECB mode with PKCS5 padding for cross-platform compatibility.
 */
public class CryptoProvider {
    private static final Logger logger = Logger.getLogger(CryptoProvider.class.getName());
    
    // In production, this key should be managed securely (e.g., Environment Variable, Key Vault)
    private static final String SECRET_KEY = "SmartPaySec123!*"; // 16 bytes for AES-128
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String ALGORITHM = "AES";

    /**
     * Encrypts biometric template before database storage or transmission.
     * @param data Raw byte array from scanner
     * @return AES-encrypted byte array
     */
    public static byte[] encrypt(byte[] data) throws Exception {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            logger.severe("Biometric encryption failed: " + e.getMessage());
            throw new RuntimeException("Security Error: Unable to protect biometric data", e);
        }
    }

    /**
     * Decrypts biometric data retrieved from database or external source.
     * @param encryptedData Encrypted byte array
     * @return Decrypted raw data
     */
    public static byte[] decrypt(byte[] encryptedData) throws Exception {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            logger.severe("Biometric decryption failed: " + e.getMessage());
            throw new RuntimeException("Security Error: Unable to recover biometric data", e);
        }
    }

    /**
     * Encodes byte array to Base64 String for API/JSON transmission.
     */
    public static String toBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decodes Base64 String back to byte array.
     */
    public static byte[] fromBase64(String base64Str) {
        return Base64.getDecoder().decode(base64Str);
    }
}
