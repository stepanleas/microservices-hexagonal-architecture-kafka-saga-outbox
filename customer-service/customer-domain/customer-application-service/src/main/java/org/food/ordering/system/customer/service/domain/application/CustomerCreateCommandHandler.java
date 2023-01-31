package org.food.ordering.system.customer.service.domain.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.customer.service.domain.core.CustomerDomainService;
import org.food.ordering.system.customer.service.domain.application.create.CreateCustomerCommand;
import org.food.ordering.system.customer.service.domain.application.ports.output.repository.CustomerRepository;
import org.food.ordering.customer.service.domain.core.entity.Customer;
import org.food.ordering.customer.service.domain.core.event.CustomerCreatedEvent;
import org.food.ordering.customer.service.domain.core.exception.CustomerDomainException;
import org.food.ordering.system.customer.service.domain.application.mapper.CustomerDataMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
class CustomerCreateCommandHandler {

    private final CustomerDomainService customerDomainService;

    private final CustomerRepository customerRepository;

    private final CustomerDataMapper customerDataMapper;



    @Transactional
    public CustomerCreatedEvent createCustomer(CreateCustomerCommand createCustomerCommand) {
        Customer customer = customerDataMapper.createCustomerCommandToCustomer(createCustomerCommand);
        CustomerCreatedEvent customerCreatedEvent = customerDomainService.validateAndInitiateCustomer(customer);
        Customer savedCustomer = customerRepository.createCustomer(customer);
        if (savedCustomer == null) {
            log.error("Could not save customer with id: {}", createCustomerCommand.customerId());
            throw new CustomerDomainException("Could not save customer with id " + createCustomerCommand.customerId());
        }
        log.info("Returning CustomerCreatedEvent for customer id: {}", createCustomerCommand.customerId());
        return customerCreatedEvent;
    }
}
