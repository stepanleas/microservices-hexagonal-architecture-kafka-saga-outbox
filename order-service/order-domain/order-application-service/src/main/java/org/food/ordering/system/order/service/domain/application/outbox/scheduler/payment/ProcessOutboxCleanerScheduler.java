package org.food.ordering.system.order.service.domain.application.outbox.scheduler.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.application.outbox.model.payment.OrderPaymentOutboxMessage;
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
public class ProcessOutboxCleanerScheduler implements OutboxScheduler {
    private final PaymentOutboxHelper paymentOutboxHelper;

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<OrderPaymentOutboxMessage>> outboxMessagesResponse = paymentOutboxHelper
            .getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.COMPLETED,
                SagaStatus.SUCCEEDED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED);

        if (outboxMessagesResponse.isPresent()) {
            List<OrderPaymentOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} {} for clean-up. The payloads: {}",
                outboxMessages.size(),
                OrderPaymentOutboxMessage.class.getSimpleName(),
                outboxMessages.stream().map(OrderPaymentOutboxMessage::getPayload).collect(Collectors.joining("\n")));

            paymentOutboxHelper.deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.COMPLETED,
                SagaStatus.SUCCEEDED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED);
            log.info("{} {} deleted!", outboxMessages.size(), OrderPaymentOutboxMessage.class.getSimpleName());
        }
    }
}
