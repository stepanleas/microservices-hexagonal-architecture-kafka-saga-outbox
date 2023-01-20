package org.food.ordering.system.saga;

import org.food.ordering.system.domain.events.DomainEvent;

public interface SagaStep<T, S extends DomainEvent, U extends DomainEvent> {
    S process(T data);
    U rollback(T data);
}
