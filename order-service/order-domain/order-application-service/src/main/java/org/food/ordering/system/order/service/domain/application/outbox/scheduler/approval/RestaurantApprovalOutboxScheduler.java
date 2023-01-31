package org.food.ordering.system.order.service.domain.application.outbox.scheduler.approval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.application.outbox.model.approval.OrderApprovalOutboxMessage;
import org.food.ordering.system.order.service.domain.application.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import org.food.ordering.system.outbox.OutboxScheduler;
import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.saga.SagaStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalOutboxScheduler implements OutboxScheduler {

    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
        initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<OrderApprovalOutboxMessage>> outboxMessagesResponse = approvalOutboxHelper
            .getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.STARTED,
                SagaStatus.PROCESSING);

        if (outboxMessagesResponse.isPresent()) {
            List<OrderApprovalOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} {} with ids: {}, sending to message bus!",
                outboxMessages.size(),
                OrderApprovalOutboxMessage.class.getSimpleName(),
                outboxMessages.stream().map(message -> message.getId().toString()).collect(Collectors.joining(",")));

            outboxMessages.forEach(outboxMessage -> {
                restaurantApprovalRequestMessagePublisher.publish(outboxMessage, this::updateOutboxStatus);
            });
            log.info("{} {} sent to message bus!", OrderApprovalOutboxMessage.class.getSimpleName(), outboxMessages.size());
        }
    }

    private void updateOutboxStatus(OrderApprovalOutboxMessage orderApprovalOutboxMessage, OutboxStatus outboxStatus) {
        orderApprovalOutboxMessage.setOutboxStatus(outboxStatus);
        approvalOutboxHelper.save(orderApprovalOutboxMessage);
        log.info("{} is updated with outbox status: {}", OrderApprovalOutboxMessage.class.getSimpleName(), outboxStatus.name());
    }
}
