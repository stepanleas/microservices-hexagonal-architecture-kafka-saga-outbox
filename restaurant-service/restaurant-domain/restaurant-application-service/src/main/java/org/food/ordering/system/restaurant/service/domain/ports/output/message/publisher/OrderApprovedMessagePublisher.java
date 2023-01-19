package org.food.ordering.system.restaurant.service.domain.ports.output.message.publisher;

import org.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import org.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;

public interface OrderApprovedMessagePublisher extends DomainEventPublisher<OrderApprovedEvent> {
}
