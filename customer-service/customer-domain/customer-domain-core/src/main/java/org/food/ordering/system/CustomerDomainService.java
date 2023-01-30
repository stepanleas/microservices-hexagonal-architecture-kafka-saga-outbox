package org.food.ordering.system;

import org.food.ordering.system.entity.Customer;
import org.food.ordering.system.event.CustomerCreatedEvent;

public interface CustomerDomainService {

    CustomerCreatedEvent validateAndInitiateCustomer(Customer customer);

}
