package org.food.ordering.system.payment.service.domain.application.mapper;

import org.food.ordering.system.domain.valueobject.CustomerId;
import org.food.ordering.system.domain.valueobject.Money;
import org.food.ordering.system.domain.valueobject.OrderId;
import org.food.ordering.system.payment.service.domain.application.dto.PaymentRequest;
import org.food.ordering.system.payment.service.domain.core.entity.Payment;
import org.food.ordering.system.payment.service.domain.core.event.PaymentEvent;
import org.food.ordering.system.payment.service.domain.application.outbox.model.OrderEventPayload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentDataMapper {
    public Payment paymentRequestModelToPayment(PaymentRequest paymentRequest) {
        return Payment.builder()
            .orderId(new OrderId(UUID.fromString(paymentRequest.getOrderId())))
            .customerId(new CustomerId(UUID.fromString(paymentRequest.getCustomerId())))
            .price(new Money(paymentRequest.getPrice()))
            .build();
    }

    public OrderEventPayload paymentEventToOrderEventPayload(PaymentEvent payment) {
        return OrderEventPayload.builder()
            .orderId(payment.getPayment().getOrderId().getValue().toString())
            .customerId(payment.getPayment().getCustomerId().getValue().toString())
            .price(payment.getPayment().getPrice().getAmount())
            .paymentId(payment.getPayment().getId().toString())
            .createdAt(payment.getCreatedAt())
            .failureMessages(payment.getFailureMessages())
            .paymentStatus(payment.getPayment().getPaymentStatus().toString())
            .build();
    }
}
