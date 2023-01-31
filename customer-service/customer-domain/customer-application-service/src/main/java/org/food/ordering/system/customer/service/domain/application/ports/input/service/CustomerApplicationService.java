package org.food.ordering.system.customer.service.domain.application.ports.input.service;

import jakarta.validation.Valid;
import org.food.ordering.system.customer.service.domain.application.create.CreateCustomerCommand;
import org.food.ordering.system.customer.service.domain.application.create.CreateCustomerResponse;

public interface CustomerApplicationService {

    CreateCustomerResponse createCustomer(@Valid CreateCustomerCommand createCustomerCommand);

}
