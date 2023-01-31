package org.food.ordering.system.order.service.domain.application.outbox.scheduler.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.domain.valueobject.OrderStatus;
import org.food.ordering.system.order.service.domain.core.exception.OrderDomainException;
import org.food.ordering.system.order.service.domain.application.outbox.model.payment.OrderPaymentEventPayload;
import org.food.ordering.system.order.service.domain.application.outbox.model.payment.OrderPaymentOutboxMessage;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.PaymentOutboxRepository;
import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.saga.SagaStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.food.ordering.system.saga.order.SagaConstant.ORDER_SAGA_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxHelper {
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
        OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        return paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatus);
    }

    @Transactional(readOnly = true)
    public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID sagaId, SagaStatus... sagaStatus) {
        return paymentOutboxRepository.findByTypeAndSagaIdAndSagaStatus(ORDER_SAGA_NAME, sagaId, sagaStatus);
    }

    @Transactional
    public void save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
        OrderPaymentOutboxMessage response = paymentOutboxRepository.save(orderPaymentOutboxMessage);
        if (response == null) {
            log.error("Could not save {} with outbox id: {}", OrderPaymentOutboxMessage.class.getSimpleName(), orderPaymentOutboxMessage.getId());
            throw new OrderDomainException("Could not save "+ OrderPaymentOutboxMessage.class.getSimpleName() +" with outbox id: " +
                orderPaymentOutboxMessage.getId());
        }
        log.info("{} saved with outbox id: {}", OrderPaymentOutboxMessage.class.getSimpleName(), response.getId());
    }

    @Transactional
    public void savePaymentOutboxMessage(OrderPaymentEventPayload orderPaymentEventPayload,
                                         OrderStatus orderStatus,
                                         SagaStatus sagaStatus,
                                         OutboxStatus outboxStatus,
                                         UUID sagaId) {
        save(OrderPaymentOutboxMessage.builder()
            .id(UUID.randomUUID())
            .sagaId(sagaId)
            .createdAt(orderPaymentEventPayload.getCreatedAt())
            .type(ORDER_SAGA_NAME)
            .payload(createPayment(orderPaymentEventPayload))
            .orderStatus(orderStatus)
            .sagaStatus(sagaStatus)
            .outboxStatus(outboxStatus)
            .build());
    }

    private String createPayment(OrderPaymentEventPayload orderPaymentEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderPaymentEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create {} object for order id: {}",
                OrderPaymentEventPayload.class.getSimpleName(), orderPaymentEventPayload.getOrderId());
            throw new OrderDomainException("Could not create " + OrderPaymentEventPayload.class.getSimpleName() +
                " object for order id: " + orderPaymentEventPayload.getOrderId(), e);
        }
    }

    @Transactional
    public void deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        paymentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatus);
    }
}
