package com.ringme.cms.service.gym;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaypalService {

    private final PayPalHttpClient payPalHttpClient;

    /**
     * Creates a one-time order using the v2 SDK.
     * @return The created Order object, which includes the approval link.
     */
    public Order createOrder(Double total, String currency, String description, String cancelUrl, String successUrl) throws IOException {
        // 1. Create the request object
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");

        // 2. Build the request body
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE"); // Critical for one-time payments

        // 3. Define the purchase unit (what the user is buying)
        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                .description(description)
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode(currency)
                        // Use Locale.US to ensure dot decimal separator, and format to 2 decimal places
                        .value(String.format(Locale.US, "%.2f", total)));

        orderRequest.purchaseUnits(List.of(purchaseUnit));

        // 4. Set the redirect URLs
        orderRequest.applicationContext(new ApplicationContext()
                .cancelUrl(cancelUrl)
                .returnUrl(successUrl));

        request.requestBody(orderRequest);

        // 5. Execute the request
        HttpResponse<Order> response = payPalHttpClient.execute(request);
        return response.result();
    }

    /**
     * Captures the payment for an order after the user has approved it.
     * This replaces the old "execute payment" logic.
     * @param orderId The ID of the order to capture (from the 'token' URL parameter).
     * @return The captured order details.
     */
    public Order captureOrder(String orderId) throws IOException {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        // An empty request body is required for the capture call.
        request.requestBody(new OrderRequest());

        HttpResponse<Order> response = payPalHttpClient.execute(request);
        return response.result();
    }

    /**
     * Finds the approval link in the Order object's links.
     * @param order The Order object returned from createOrder.
     * @return An Optional containing the approval link URL.
     */
    public Optional<String> getApprovalLink(Order order) {
        return order.links().stream()
                // The new relation type is "approve", not "approval_url"
                .filter(link -> "approve".equalsIgnoreCase(link.rel()))
                .map(LinkDescription::href)
                .findFirst();
    }
}