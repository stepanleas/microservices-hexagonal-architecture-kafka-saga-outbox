package org.food.ordering.system.order.service.domain.core;

import org.food.ordering.system.order.service.domain.core.entity.Order;
import org.food.ordering.system.order.service.domain.core.entity.Restaurant;
import org.food.ordering.system.order.service.domain.core.event.OrderCancelledEvent;
import org.food.ordering.system.order.service.domain.core.event.OrderCreatedEvent;
import org.food.ordering.system.order.service.domain.core.event.OrderPaidEvent;

import java.util.List;

public interface OrderDomainService {
    OrderCreatedEvent validatedAndInitiateOrder(Order order, Restaurant restaurant);
    OrderPaidEvent payOrder(Order order);
    void approveOrder(Order order);
    OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages);
    void cancelOrder(Order order, List<String> failureMessages);
}
