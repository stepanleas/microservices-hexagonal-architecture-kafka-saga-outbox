package org.food.ordering.system.order.service.domain.application.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.domain.valueobject.OrderId;
import org.food.ordering.system.domain.valueobject.OrderStatus;
import org.food.ordering.system.order.service.domain.core.entity.Order;
import org.food.ordering.system.order.service.domain.core.exception.OrderNotFoundException;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.OrderRepository;
import org.food.ordering.system.saga.SagaStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaHelper {

    private final OrderRepository orderRepository;

    Order findOrder(String orderId) {
        Optional<Order> orderResponse = orderRepository.findById(new OrderId(UUID.fromString(orderId)));
        if (orderResponse.isEmpty()) {
            log.error("Order with id: {} could not be found!", orderId);
            throw new OrderNotFoundException("Order with id: " + orderId + " could not be found!");
        }

        return orderResponse.get();
    }

    void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public SagaStatus orderStatusToSagaStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PAID -> SagaStatus.PROCESSING;
            case APPROVED -> SagaStatus.SUCCEEDED;
            case CANCELLING -> SagaStatus.COMPENSATING;
            case CANCELLED -> SagaStatus.COMPENSATED;
            default -> SagaStatus.STARTED;
        };
    }
}
