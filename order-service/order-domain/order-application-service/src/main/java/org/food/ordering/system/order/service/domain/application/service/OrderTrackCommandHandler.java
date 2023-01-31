package org.food.ordering.system.order.service.domain.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.application.dto.track.TrackOrderQuery;
import org.food.ordering.system.order.service.domain.application.dto.track.TrackOrderResponse;
import org.food.ordering.system.order.service.domain.application.mapper.OrderDataMapper;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.OrderRepository;
import org.food.ordering.system.order.service.domain.core.entity.Order;
import org.food.ordering.system.order.service.domain.core.exception.OrderNotFoundException;
import org.food.ordering.system.order.service.domain.core.valueobject.TrackingId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTrackCommandHandler {
    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        Optional<Order> order = orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));
        if (order.isEmpty()) {
            log.info("Could not find order with tracking id: {}", trackOrderQuery.getOrderTrackingId());
            throw new OrderNotFoundException("Could not find order with tracking id: " + trackOrderQuery.getOrderTrackingId());
        }

        return orderDataMapper.orderToTrackOrderResponse(order.get());
    }
}
