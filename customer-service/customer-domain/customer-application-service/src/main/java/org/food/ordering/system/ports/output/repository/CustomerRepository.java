package org.food.ordering.system.ports.output.repository;

import org.food.ordering.system.entity.Customer;

public interface CustomerRepository {

    Customer createCustomer(Customer customer);
}
