package org.food.ordering.system.order.service.domain.application.ports.input.message.listener.customer;

import org.food.ordering.system.order.service.domain.application.dto.message.CustomerModel;

public interface CustomerMessageListener {
    void customerCreated(CustomerModel customerModel);
}
