package org.food.ordering.system.domain.events;

public interface DomainEvent<T> {
    void fire();
}
