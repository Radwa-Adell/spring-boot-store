package com.radwa.store.payments;

import com.radwa.store.carts.CartEmptyException;
import com.radwa.store.carts.CartNotFoundException;
import com.radwa.store.common.ErrorDto;
import com.radwa.store.orders.OrderRepository;
import com.radwa.store.orders.PaymentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;
    private final OrderRepository orderRepository;

    @PostMapping
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(request);
    }

    @PostMapping("/webhook")
    public void handleWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String payload
    ) {
        checkoutService.handleWebhookEvent(new WebhookRequest(headers, payload));
    }

    @PostMapping("/webhook/test")
    public void handleTestWebhook(@RequestBody Map<String, Object> payload) {
        Long orderId = ((Number) payload.get("orderId")).longValue();
        String status = payload.get("paymentStatus").toString();

        PaymentStatus paymentStatus = PaymentStatus.valueOf(status);

        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentException("Order not found"));

        order.setStatus(paymentStatus);
        orderRepository.save(order);
    }


    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<?> handlePaymentException() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("Error creating a checkout session"));
    }


    @ExceptionHandler({CartNotFoundException.class, CartEmptyException.class})
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        return ResponseEntity.badRequest().body(new ErrorDto(ex.getMessage()));
    }
}