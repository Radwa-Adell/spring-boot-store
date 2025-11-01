package com.radwa.store.payments;

import com.radwa.store.auth.AuthService;
import com.radwa.store.carts.CartEmptyException;
import com.radwa.store.carts.CartNotFoundException;
import com.radwa.store.carts.CartRepository;
import com.radwa.store.carts.CartService;
import com.radwa.store.orders.Order;
import com.radwa.store.orders.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class CheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final CartService cartService;
    private final PaymentGateway paymentGateway;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        var cart = cartRepository.getCartWithItems(request.getCartId()).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }

        if (cart.isEmpty()) {
            throw new CartEmptyException();
        }

        var order = Order.fromCart(cart, authService.getCurrentUser());

        orderRepository.save(order);

        try {
            var session = paymentGateway.createCheckoutSession(order);

            cartService.clearCart(cart.getId());

            return new CheckoutResponse(order.getId(), session.getCheckoutUrl());
        } catch (PaymentException ex) {
            orderRepository.delete(order);
            throw ex;
        }
    }

    public void handleWebhookEvent(WebhookRequest request) {
        paymentGateway
                .parseWebhookRequest(request)
                .ifPresent(paymentResult -> {
                    System.out.println("Parsed PaymentResult: orderId=" + paymentResult.getOrderId() + ", status=" + paymentResult.getPaymentStatus());
                    var order = orderRepository.findById(paymentResult.getOrderId()).orElseThrow(() -> new PaymentException("Order not found"));
                    System.out.println("Found Order ID: " + order.getId() + " | Current Status: " + order.getStatus());
                    order.setStatus(paymentResult.getPaymentStatus());
                    orderRepository.save(order);
                    System.out.println("Updated order #" + order.getId() + " to status " + paymentResult.getPaymentStatus());
                });
    }

}