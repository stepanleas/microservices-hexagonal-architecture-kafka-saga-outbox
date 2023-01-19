package org.food.ordering.system.restaurant.service.domain.ports.output.message.publisher;

import org.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import org.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import org.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;

public interface OrderRejectedMessagePublisher extends DomainEventPublisher<OrderRejectedEvent> {
}
