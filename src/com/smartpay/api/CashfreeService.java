package com.smartpay.api;

import org.json.JSONObject;
import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Service to handle HTTPS-only integration with Cashfree Payment Gateway.
 */
public class CashfreeService {
    private static final Logger logger = Logger.getLogger(CashfreeService.class.getName());

    public String createOrder(String orderId, double amount, String customerId, String customerPhone) throws Exception {
        URL url = new URL(CashfreeConfig.BASE_URL + "/orders");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-api-version", CashfreeConfig.API_VERSION);
        conn.setRequestProperty("x-client-id", CashfreeConfig.APP_ID);
        conn.setRequestProperty("x-client-secret", CashfreeConfig.SECRET_KEY);
        conn.setDoOutput(true);

        JSONObject payload = new JSONObject();
        payload.put("order_id", orderId);
        payload.put("order_amount", amount);
        payload.put("order_currency", "INR");
        
        JSONObject customerDetails = new JSONObject();
        customerDetails.put("customer_id", customerId);
        customerDetails.put("customer_phone", customerPhone);
        
        payload.put("customer_details", customerDetails);

        logger.info("Sending HTTPS request to Cashfree: " + orderId);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                logger.info("Cashfree Order Created: " + orderId);
                return response;
            }
        } else {
            try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8.name())) {
                String errorMsg = scanner.useDelimiter("\\A").next();
                logger.severe("Cashfree API Error (" + responseCode + "): " + errorMsg);
                throw new Exception("Cashfree Payment Gateway Error: " + errorMsg);
            }
        }
    }
}
