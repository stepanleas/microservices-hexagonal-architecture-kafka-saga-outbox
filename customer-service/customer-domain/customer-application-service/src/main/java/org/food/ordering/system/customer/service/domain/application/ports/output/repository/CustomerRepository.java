package org.food.ordering.system.customer.service.domain.application.ports.output.repository;

import org.food.ordering.customer.service.domain.core.entity.Customer;

public interface CustomerRepository {

    Customer createCustomer(Customer customer);
}
