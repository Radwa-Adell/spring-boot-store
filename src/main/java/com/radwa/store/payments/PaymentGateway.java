package com.radwa.store.payments;

import com.radwa.store.orders.Order;

import java.util.Optional;

public interface PaymentGateway {

    CheckoutSession createCheckoutSession(Order order);

    Optional<PaymentResult> parseWebhookRequest(WebhookRequest request);
}