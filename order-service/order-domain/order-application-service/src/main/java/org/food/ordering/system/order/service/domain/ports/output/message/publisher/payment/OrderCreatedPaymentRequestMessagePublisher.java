package org.food.ordering.system.order.service.domain.ports.output.message.publisher.payment;

import org.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import org.food.ordering.system.order.service.domain.event.OrderCreatedEvent;

public interface OrderCreatedPaymentRequestMessagePublisher extends DomainEventPublisher<OrderCreatedEvent> {
}
