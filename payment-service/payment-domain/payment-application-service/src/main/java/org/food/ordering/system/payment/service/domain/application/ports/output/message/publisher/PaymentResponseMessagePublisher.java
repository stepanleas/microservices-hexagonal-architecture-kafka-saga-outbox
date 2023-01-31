package org.food.ordering.system.payment.service.domain.application.ports.output.message.publisher;

import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.payment.service.domain.application.outbox.model.OrderOutboxMessage;

import java.util.function.BiConsumer;

public interface PaymentResponseMessagePublisher {
    void publish(OrderOutboxMessage message, BiConsumer<OrderOutboxMessage , OutboxStatus> outboxCallback);
}
