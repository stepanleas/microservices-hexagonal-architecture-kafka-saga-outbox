package org.food.ordering.system.ports.input.service;

import jakarta.validation.Valid;
import org.food.ordering.system.create.CreateCustomerCommand;
import org.food.ordering.system.create.CreateCustomerResponse;

public interface CustomerApplicationService {

    CreateCustomerResponse createCustomer(@Valid CreateCustomerCommand createCustomerCommand);

}
