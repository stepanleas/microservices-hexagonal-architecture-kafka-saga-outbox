package org.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
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
public class PaymentOutboxScheduler implements OutboxScheduler {

    private final PaymentOutboxHelper paymentOutboxHelper;
    private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
        initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<OrderPaymentOutboxMessage>> outboxMessagesResponse = paymentOutboxHelper
            .getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.STARTED, SagaStatus.STARTED, SagaStatus.COMPENSATING);

        if (outboxMessagesResponse.isPresent() && outboxMessagesResponse.get().size() > 0) {
            List<OrderPaymentOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} {} with ids: {}, sending to message bus!",
                outboxMessages.size(),
                OrderPaymentOutboxMessage.class.getSimpleName(),
                outboxMessages.stream().map(outboxMessage -> outboxMessage.getId().toString()).collect(Collectors.joining(",")));

            outboxMessages.forEach(outboxMessage -> {
                paymentRequestMessagePublisher.publish(outboxMessage, this::updateOutboxStatus);
            });
            log.info("{} {} sent to message bus!", outboxMessages.size(), OrderPaymentOutboxMessage.class.getSimpleName());
        }
    }

    private void updateOutboxStatus(OrderPaymentOutboxMessage orderPaymentOutboxMessage, OutboxStatus outboxStatus) {
        orderPaymentOutboxMessage.setOutboxStatus(outboxStatus);
        paymentOutboxHelper.save(orderPaymentOutboxMessage);
        log.info("{} is updated with outbox status: {}", OrderPaymentOutboxMessage.class.getSimpleName(), outboxStatus.name());
    }
}
