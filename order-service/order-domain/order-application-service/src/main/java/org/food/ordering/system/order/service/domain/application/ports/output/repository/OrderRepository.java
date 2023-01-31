package org.food.ordering.system.order.service.domain.application.ports.output.repository;

import org.food.ordering.system.domain.valueobject.OrderId;
import org.food.ordering.system.order.service.domain.core.entity.Order;
import org.food.ordering.system.order.service.domain.core.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId orderId);
    Optional<Order> findByTrackingId(TrackingId trackingId);
}
