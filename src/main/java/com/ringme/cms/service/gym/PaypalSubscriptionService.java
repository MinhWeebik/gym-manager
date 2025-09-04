package com.ringme.cms.service.gym;

import com.fasterxml.jackson.databind.JsonNode;
import com.paypal.core.PayPalHttpClient;
import com.ringme.cms.config.PaypalConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
@Log4j2
public class PaypalSubscriptionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PaypalConfig paypalConfig;

    public JsonNode createSubscription(String planId, String returnUrl, String cancelUrl) {
        String accessToken = paypalConfig.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        // Using a Java Text Block for clean, readable JSON
        String requestJson = String.format("""
        {
          "plan_id": "%s",
          "application_context": {
            "brand_name": "Your Awesome Service",
            "return_url": "%s",
            "cancel_url": "%s",
            "user_action": "SUBSCRIBE_NOW"
          }
        }
        """, planId, returnUrl, cancelUrl);

        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
        String url = paypalConfig.getBaseUrl() + "/v1/billing/subscriptions";

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, request, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Successfully created manual PayPal subscription with ID: {}", response.getBody().path("id").asText());
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to create PayPal subscription for plan {}", planId, e);
            throw new RuntimeException("Failed to create PayPal subscription", e);
        }
        throw new RuntimeException("Failed to create PayPal subscription");
    }

    public JsonNode getSubscriptionDetails(String subscriptionId) {
        String accessToken = paypalConfig.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = paypalConfig.getBaseUrl() + "/v1/billing/subscriptions/" + subscriptionId;

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to get details for subscription {}", subscriptionId, e);
            throw new RuntimeException("Failed to get subscription details", e);
        }
        throw new RuntimeException("Failed to get subscription details");
    }

    public void suspendSubscription(String subscriptionId, String reason) {
        String accessToken = paypalConfig.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        String requestJson = String.format("""
            {
              "reason": "%s"
            }
            """, reason);

        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
        String url = paypalConfig.getBaseUrl() + "/v1/billing/subscriptions/" + subscriptionId + "/suspend";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("Successfully suspended subscription {}", subscriptionId);
            } else {
                log.warn("Received unexpected status {} when suspending subscription {}", response.getStatusCode(), subscriptionId);
            }
        } catch (Exception e) {
            log.error("Failed to suspend subscription {}", subscriptionId, e);
            throw new RuntimeException("Failed to suspend subscription", e);
        }
    }

    public void activateSubscription(String subscriptionId, String reason) {
        String accessToken = paypalConfig.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        String requestJson = String.format("""
            {
              "reason": "%s"
            }
            """, reason);

        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
        String url = paypalConfig.getBaseUrl() + "/v1/billing/subscriptions/" + subscriptionId + "/activate";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("Successfully activated subscription {}", subscriptionId);
            } else {
                log.warn("Received unexpected status {} when activating subscription {}", response.getStatusCode(), subscriptionId);
            }
        } catch (Exception e) {
            log.error("Failed to activate subscription {}", subscriptionId, e);
            throw new RuntimeException("Failed to activate subscription", e);
        }
    }

    public void cancelSubscription(String subscriptionId, String reason) {
        String accessToken = paypalConfig.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        // Create the simple JSON body with the required reason.
        String requestJson = String.format("""
        {
          "reason": "%s"
        }
        """, reason);

        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
        String url = paypalConfig.getBaseUrl() + "/v1/billing/subscriptions/" + subscriptionId + "/cancel";

        try {
            // A POST request is made to the /cancel endpoint.
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("Successfully PERMANENTLY canceled subscription {}", subscriptionId);
            } else {
                // This case is unlikely but good to handle.
                log.warn("Received unexpected status {} when canceling subscription {}", response.getStatusCode(), subscriptionId);
                throw new RuntimeException("Failed to cancel subscription with unexpected status.");
            }
        } catch (Exception e) {
            log.error("Failed to cancel subscription {}", subscriptionId, e);
            throw new RuntimeException("Failed to cancel subscription", e);
        }
    }
}
