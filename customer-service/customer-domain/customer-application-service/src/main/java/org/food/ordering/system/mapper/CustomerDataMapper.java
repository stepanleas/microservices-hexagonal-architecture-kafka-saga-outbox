package org.food.ordering.system.mapper;

import org.food.ordering.system.create.CreateCustomerCommand;
import org.food.ordering.system.create.CreateCustomerResponse;
import org.food.ordering.system.domain.valueobject.CustomerId;
import org.food.ordering.system.entity.Customer;
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
