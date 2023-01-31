package org.food.ordering.system.customer.service.domain.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.customer.service.domain.application.create.CreateCustomerCommand;
import org.food.ordering.system.customer.service.domain.application.create.CreateCustomerResponse;
import org.food.ordering.system.customer.service.domain.application.ports.output.message.publisher.CustomerMessagePublisher;
import org.food.ordering.customer.service.domain.core.event.CustomerCreatedEvent;
import org.food.ordering.system.customer.service.domain.application.mapper.CustomerDataMapper;
import org.food.ordering.system.customer.service.domain.application.ports.input.service.CustomerApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
class CustomerApplicationServiceImpl implements CustomerApplicationService {

    private final CustomerCreateCommandHandler customerCreateCommandHandler;

    private final CustomerDataMapper customerDataMapper;

    private final CustomerMessagePublisher customerMessagePublisher;


    @Override
    public CreateCustomerResponse createCustomer(CreateCustomerCommand createCustomerCommand) {
        CustomerCreatedEvent customerCreatedEvent = customerCreateCommandHandler.createCustomer(createCustomerCommand);
        customerMessagePublisher.publish(customerCreatedEvent);
        return customerDataMapper
                .customerToCreateCustomerResponse(customerCreatedEvent.getCustomer(),
                        "Customer saved successfully!");
    }
}
