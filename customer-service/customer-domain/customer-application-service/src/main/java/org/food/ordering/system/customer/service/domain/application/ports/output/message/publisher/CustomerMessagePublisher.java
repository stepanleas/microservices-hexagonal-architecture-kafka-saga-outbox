package org.food.ordering.system.customer.service.domain.application.ports.output.message.publisher;

import org.food.ordering.customer.service.domain.core.event.CustomerCreatedEvent;

public interface CustomerMessagePublisher {

    void publish(CustomerCreatedEvent customerCreatedEvent);

}