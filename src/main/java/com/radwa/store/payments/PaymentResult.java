package com.radwa.store.payments;

import com.radwa.store.orders.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentResult {

    private Long orderId;
    private PaymentStatus paymentStatus;
}
