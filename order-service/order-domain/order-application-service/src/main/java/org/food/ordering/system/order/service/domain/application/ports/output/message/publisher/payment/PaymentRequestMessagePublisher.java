package org.food.ordering.system.order.service.domain.application.ports.output.message.publisher.payment;

import org.food.ordering.system.order.service.domain.application.outbox.model.payment.OrderPaymentOutboxMessage;
import org.food.ordering.system.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface PaymentRequestMessagePublisher {
    void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
                 BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCall);
}
