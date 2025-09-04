package com.ringme.cms.config;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;
import java.util.Map;

@Configuration
@Log4j2
public class PaypalConfig {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${paypal.client.id}")
    private String clientId;
    @Value("${paypal.client.secret}")
    private String clientSecret;
    @Value("${paypal.mode}")
    private String mode;
    private String accessToken;

    @Bean
    public PayPalHttpClient payPalHttpClient() {
        PayPalEnvironment environment;
        if ("live".equals(mode)) {
            environment = new PayPalEnvironment.Live(clientId, clientSecret);
        } else {
            // Default to sandbox
            environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
        }
        return new PayPalHttpClient(environment);
    }

    public String getAccessToken() {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String authHeader = "Basic " + encodedAuth;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authHeader);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        String url = getBaseUrl() + "/v1/oauth2/token";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                this.accessToken = (String) response.getBody().get("access_token");
                log.info("Successfully retrieved PayPal access token.");
                return this.accessToken;
            }
        } catch (Exception e) {
            log.error("Failed to get PayPal access token", e);
            throw new RuntimeException("Failed to get PayPal access token", e);
        }
        throw new RuntimeException("Failed to get PayPal access token");
    }

    public String getBaseUrl() {
        return "live".equals(mode) ? "https://api.paypal.com" : "https://api.sandbox.paypal.com";
    }


}
