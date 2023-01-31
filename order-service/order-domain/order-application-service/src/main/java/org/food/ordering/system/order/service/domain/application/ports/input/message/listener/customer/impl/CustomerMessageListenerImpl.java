package org.food.ordering.system.order.service.domain.application.ports.input.message.listener.customer.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.application.dto.message.CustomerModel;
import org.food.ordering.system.order.service.domain.application.mapper.OrderDataMapper;
import org.food.ordering.system.order.service.domain.application.ports.input.message.listener.customer.CustomerMessageListener;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.CustomerRepository;
import org.food.ordering.system.order.service.domain.core.entity.Customer;
import org.food.ordering.system.order.service.domain.core.exception.OrderDomainException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerMessageListenerImpl implements CustomerMessageListener {

    private final CustomerRepository customerRepository;
    private final OrderDataMapper orderDataMapper;

    @Override
    public void customerCreated(CustomerModel customerModel) {
        Customer customer = customerRepository.save(orderDataMapper.customerModelToCustomer(customerModel));
        if (customer == null) {
            log.error("Customer could not be created in order database with id: {}", customerModel.getId());
            throw new OrderDomainException("Customer could not be created in order database with id: " + customerModel.getId());
        }

        log.info("Customer is created in order database with id: {}", customer.getId().getValue());
    }
}
