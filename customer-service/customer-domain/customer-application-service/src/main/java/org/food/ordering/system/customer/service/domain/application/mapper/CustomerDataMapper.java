package org.food.ordering.system.customer.service.domain.application.mapper;

import org.food.ordering.system.customer.service.domain.application.create.CreateCustomerCommand;
import org.food.ordering.system.customer.service.domain.application.create.CreateCustomerResponse;
import org.food.ordering.system.domain.valueobject.CustomerId;
import org.food.ordering.customer.service.domain.core.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataMapper {

    public Customer createCustomerCommandToCustomer(CreateCustomerCommand createCustomerCommand) {
        return new Customer(new CustomerId(createCustomerCommand.customerId()),
                createCustomerCommand.username(),
                createCustomerCommand.firstName(),
                createCustomerCommand.lastName());
    }

    public CreateCustomerResponse customerToCreateCustomerResponse(Customer customer, String message) {
        return new CreateCustomerResponse(customer.getId().getValue(), message);
    }
}
