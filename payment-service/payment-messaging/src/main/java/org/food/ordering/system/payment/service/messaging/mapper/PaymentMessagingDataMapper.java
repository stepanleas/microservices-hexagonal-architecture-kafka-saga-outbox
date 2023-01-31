package org.food.ordering.system.payment.service.messaging.mapper;

import org.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import org.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import org.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import org.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import org.food.ordering.system.payment.service.domain.application.dto.PaymentRequest;
import org.food.ordering.system.payment.service.domain.application.outbox.model.OrderEventPayload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentMessagingDataMapper {

    public PaymentRequest paymentRequestAvroModelToPaymentRequest(PaymentRequestAvroModel paymentRequestAvroModel) {
        return PaymentRequest.builder()
            .id(paymentRequestAvroModel.getId())
            .sagaId(paymentRequestAvroModel.getSagaId())
            .customerId(paymentRequestAvroModel.getCustomerId())
            .orderId(paymentRequestAvroModel.getOrderId())
            .price(paymentRequestAvroModel.getPrice())
            .createdAt(paymentRequestAvroModel.getCreatedAt())
            .paymentOrderStatus(PaymentOrderStatus.valueOf(paymentRequestAvroModel.getPaymentOrderStatus().name()))
            .build();
    }

    public PaymentResponseAvroModel orderEventPayloadToPaymentResponseAvroModel(String sagaId, OrderEventPayload orderEventPayload) {
        return PaymentResponseAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId(sagaId)
            .setPaymentId(orderEventPayload.getPaymentId())
            .setCustomerId(orderEventPayload.getCustomerId())
            .setOrderId(orderEventPayload.getOrderId())
            .setPrice(orderEventPayload.getPrice())
            .setCreatedAt(orderEventPayload.getCreatedAt().toInstant())
            .setPaymentStatus(PaymentStatus.valueOf(orderEventPayload.getPaymentStatus()))
            .setFailureMessages(orderEventPayload.getFailureMessages())
            .build();
    }
}
