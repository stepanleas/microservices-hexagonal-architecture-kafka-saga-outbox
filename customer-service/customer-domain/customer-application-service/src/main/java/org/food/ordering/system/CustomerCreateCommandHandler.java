package org.food.ordering.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.create.CreateCustomerCommand;
import org.food.ordering.system.entity.Customer;
import org.food.ordering.system.event.CustomerCreatedEvent;
import org.food.ordering.system.exception.CustomerDomainException;
import org.food.ordering.system.mapper.CustomerDataMapper;
import org.food.ordering.system.ports.output.repository.CustomerRepository;
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
