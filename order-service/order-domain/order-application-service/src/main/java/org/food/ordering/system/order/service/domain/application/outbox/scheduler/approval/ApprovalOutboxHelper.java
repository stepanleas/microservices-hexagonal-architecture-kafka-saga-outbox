package org.food.ordering.system.order.service.domain.application.outbox.scheduler.approval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.domain.valueobject.OrderStatus;
import org.food.ordering.system.order.service.domain.core.exception.OrderDomainException;
import org.food.ordering.system.order.service.domain.application.outbox.model.approval.OrderApprovalEventPayload;
import org.food.ordering.system.order.service.domain.application.outbox.model.approval.OrderApprovalOutboxMessage;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.ApprovalOutboxRepository;
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
public class ApprovalOutboxHelper {
    private final ApprovalOutboxRepository approvalOutboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Optional<List<OrderApprovalOutboxMessage>> getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
        OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        return approvalOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatus);
    }

    public Optional<OrderApprovalOutboxMessage> getApprovalOutboxMessageBySagaIdAndSagaStatus(
        UUID sagaId, SagaStatus... sagaStatus) {
        return approvalOutboxRepository.findByTypeAndSagaIdAndSagaStatus(ORDER_SAGA_NAME, sagaId, sagaStatus);
    }

    @Transactional
    public void save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
        OrderApprovalOutboxMessage response = approvalOutboxRepository.save(orderApprovalOutboxMessage);
        if (response == null) {
            log.error("Could not save {} with outbox id: {}",
                OrderApprovalOutboxMessage.class.getSimpleName(),
                orderApprovalOutboxMessage.getId());
            throw new OrderDomainException("Could not save " + OrderApprovalOutboxMessage.class.getSimpleName() +
                " with outbox id: " + orderApprovalOutboxMessage.getId());
        }
        log.info("{} saved with outbox id: {}", OrderApprovalOutboxMessage.class.getSimpleName(), orderApprovalOutboxMessage.getId());
    }

    @Transactional
    public void deleteApprovalOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        approvalOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatus);
    }

    @Transactional
    public void saveApprovalOutboxMessage(OrderApprovalEventPayload orderApprovalEventPayload,
                                          OrderStatus orderStatus,
                                          SagaStatus sagaStatus,
                                          OutboxStatus outboxStatus,
                                          UUID sagaId) {
        save(OrderApprovalOutboxMessage.builder()
            .id(UUID.randomUUID())
            .sagaId(sagaId)
            .createdAt(orderApprovalEventPayload.getCreatedAt())
            .type(ORDER_SAGA_NAME)
            .payload(createPayload(orderApprovalEventPayload))
            .orderStatus(orderStatus)
            .sagaStatus(sagaStatus)
            .outboxStatus(outboxStatus)
            .build());
    }

    private String createPayload(OrderApprovalEventPayload orderApprovalEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderApprovalEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create {} for order id: {}",
                OrderApprovalEventPayload.class.getSimpleName(), orderApprovalEventPayload.getOrderId());
            throw new OrderDomainException("Could not create " + OrderApprovalEventPayload.class.getSimpleName()
                + " for order id: " + orderApprovalEventPayload.getOrderId(), e);
        }
    }
}
