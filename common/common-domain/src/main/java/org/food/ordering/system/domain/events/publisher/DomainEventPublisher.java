package org.food.ordering.system.domain.events.publisher;

import org.food.ordering.system.domain.events.DomainEvent;

public interface DomainEventPublisher<T extends DomainEvent> {
    void publish(T domainEvent);
}
