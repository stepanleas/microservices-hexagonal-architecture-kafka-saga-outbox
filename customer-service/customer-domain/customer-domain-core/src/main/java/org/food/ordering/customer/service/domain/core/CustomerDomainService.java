package org.food.ordering.customer.service.domain.core;

import org.food.ordering.customer.service.domain.core.entity.Customer;
import org.food.ordering.customer.service.domain.core.event.CustomerCreatedEvent;

public interface CustomerDomainService {

    CustomerCreatedEvent validateAndInitiateCustomer(Customer customer);

}
