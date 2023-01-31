package org.food.ordering.system.restaurant.service.domain.application.ports.output.message.publisher;

import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.restaurant.service.domain.application.outbox.model.OrderOutboxMessage;

import java.util.function.BiConsumer;

public interface RestaurantApprovalResponseMessagePublisher {

    void publish(OrderOutboxMessage orderOutboxMessage,
                 BiConsumer<OrderOutboxMessage, OutboxStatus> outboxCallback);
}
