package org.food.ordering.system.order.service.domain.application.ports.output.repository;

import org.food.ordering.system.order.service.domain.core.entity.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Optional<Customer> findCustomer(UUID customerId);
    Customer save(Customer customer);
}
