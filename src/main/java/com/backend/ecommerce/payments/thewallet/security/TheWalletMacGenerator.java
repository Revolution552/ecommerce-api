package com.backend.ecommerce.payments.thewallet.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;

@Component
public class TheWalletMacGenerator {

    private static final Logger log = LoggerFactory.getLogger(TheWalletMacGenerator.class);
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Value("${thewallet.shared-secret}")
    private String sharedSecret;

    /**
     * Generates the MAC for a request to TheWallet.
     * Composite value: msisdn:date:secret
     * @param msisdn The MSISDN for the transaction.
     * @param date The date of the transaction (LOCAL TIME OF TRANSACTION).
     * @return The SHA256 MAC as a hexadecimal string.
     */
    public String generateMac(String msisdn, LocalDateTime date) {
        String formattedDate = date.format(DATE_FORMATTER);
        String compositeValue = msisdn + ":" + formattedDate + ":" + sharedSecret;
        return hmacSha256(compositeValue, sharedSecret); // Document says secret in composite AND as key. Need to clarify which. Using sharedSecret as key.
    }

    /**
     * Validates an incoming MAC from TheWallet notification.
     * Composite value: msisdn:date:secret
     * @param receivedMac The MAC received in the notification.
     * @param msisdn The MSISDN from the notification.
     * @param date The date from the notification.
     * @return True if the MAC is valid, false otherwise.
     */
    public boolean verifyMac(String receivedMac, String msisdn, LocalDateTime date) {
        String generatedMac = generateMac(msisdn, date);
        boolean isValid = generatedMac.equalsIgnoreCase(receivedMac);
        if (!isValid) {
            log.warn("MAC verification failed. Received: '{}', Computed: '{}' for MSISDN: {}", receivedMac, generatedMac, msisdn);
        } else {
            log.info("MAC verification successful for MSISDN: {}", msisdn);
        }
        return isValid;
    }

    private String hmacSha256(String data, String key) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, HMAC_SHA256_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hexadecimal string
            try (Formatter formatter = new Formatter()) {
                for (byte b : hmacBytes) {
                    formatter.format("%02x", b);
                }
                return formatter.toString();
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error during HMAC-SHA256 calculation: {}", e.getMessage(), e);
            throw new RuntimeException("MAC generation failed", e);
        }
    }
}