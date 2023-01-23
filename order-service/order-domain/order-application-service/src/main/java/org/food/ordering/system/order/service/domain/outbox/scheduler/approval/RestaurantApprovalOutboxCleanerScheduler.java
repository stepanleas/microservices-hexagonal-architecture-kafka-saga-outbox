package org.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.food.ordering.system.outbox.OutboxScheduler;
import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.saga.SagaStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalOutboxCleanerScheduler implements OutboxScheduler {

    private final ApprovalOutboxHelper approvalOutboxHelper;

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<OrderApprovalOutboxMessage>> outboxMessagesResponse = approvalOutboxHelper
            .getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.COMPLETED,
                SagaStatus.SUCCEEDED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED);

        if (outboxMessagesResponse.isPresent()) {
            List<OrderApprovalOutboxMessage> outboxMessages = outboxMessagesResponse.get();

            log.info("Received {} {} for clean-up. The payloads: {}",
                outboxMessages.size(),
                OrderApprovalOutboxMessage.class.getSimpleName(),
                outboxMessages.stream().map(OrderApprovalOutboxMessage::getPayload).collect(Collectors.joining("\n")));

            approvalOutboxHelper.deleteApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.COMPLETED,
                SagaStatus.SUCCEEDED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED);
            log.info("{} {} deleted!", outboxMessages.size(), OrderApprovalOutboxMessage.class.getSimpleName());
        }
    }
}
