package org.food.ordering.customer.service.domain.core;

import lombok.extern.slf4j.Slf4j;
import org.food.ordering.customer.service.domain.core.entity.Customer;
import org.food.ordering.customer.service.domain.core.event.CustomerCreatedEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
public class CustomerDomainServiceImpl implements CustomerDomainService {

    public CustomerCreatedEvent validateAndInitiateCustomer(Customer customer) {
        //Any Business logic required to run for a customer creation
        log.info("Customer with id: {} is initiated", customer.getId().getValue());
        return new CustomerCreatedEvent(customer, ZonedDateTime.now(ZoneId.of("UTC")));
    }
}

