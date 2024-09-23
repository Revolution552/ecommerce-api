package com.backend.ecommerce.payment.config;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public Map<String, String> paypalSdkConfig() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("mode", mode);
        return configMap;
    }

    @Bean
    public OAuthTokenCredential oAuthTokenCredential() {
        try {
            return new OAuthTokenCredential(clientId, clientSecret, paypalSdkConfig());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create OAuthTokenCredential", e);
        }
    }

    @Bean
    public APIContext apiContext() {
        try {
            OAuthTokenCredential credential = oAuthTokenCredential();
            String accessToken = credential.getAccessToken();
            APIContext context = new APIContext(accessToken);
            context.setConfigurationMap(paypalSdkConfig());
            return context;
        } catch (PayPalRESTException e) {
            throw new RuntimeException("Failed to create PayPal APIContext: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("General error while creating PayPal APIContext: " + e.getMessage(), e);
        }
    }
}
