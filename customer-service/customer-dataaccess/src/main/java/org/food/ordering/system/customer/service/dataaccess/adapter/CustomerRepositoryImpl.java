package org.food.ordering.system.customer.service.dataaccess.adapter;

import org.food.ordering.system.customer.service.dataaccess.repository.CustomerJpaRepository;
import org.food.ordering.customer.service.domain.core.entity.Customer;
import org.food.ordering.system.customer.service.dataaccess.mapper.CustomerDataAccessMapper;
import org.food.ordering.system.customer.service.domain.application.ports.output.repository.CustomerRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;

    private final CustomerDataAccessMapper customerDataAccessMapper;

    public CustomerRepositoryImpl(CustomerJpaRepository customerJpaRepository,
                                  CustomerDataAccessMapper customerDataAccessMapper) {
        this.customerJpaRepository = customerJpaRepository;
        this.customerDataAccessMapper = customerDataAccessMapper;
    }

    @Override
    public Customer createCustomer(Customer customer) {
        return customerDataAccessMapper.customerEntityToCustomer(
                customerJpaRepository.save(customerDataAccessMapper.customerToCustomerEntity(customer)));
    }
}
