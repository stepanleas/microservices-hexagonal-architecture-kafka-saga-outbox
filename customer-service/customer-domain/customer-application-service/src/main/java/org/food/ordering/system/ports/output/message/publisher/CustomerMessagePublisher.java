package org.food.ordering.system.ports.output.message.publisher;

import org.food.ordering.system.event.CustomerCreatedEvent;

public interface CustomerMessagePublisher {

    void publish(CustomerCreatedEvent customerCreatedEvent);

}