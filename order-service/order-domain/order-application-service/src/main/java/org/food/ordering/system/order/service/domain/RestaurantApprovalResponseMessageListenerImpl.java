package org.food.ordering.system.order.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import org.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import org.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static org.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalMessageListener {

    private final OrderApprovalSaga orderApprovalSaga;

    @Override
    public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {
        orderApprovalSaga.process(restaurantApprovalResponse);
        log.info("Order is approved for order id: {}", restaurantApprovalResponse.getOrderId());
    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {
        OrderCancelledEvent orderCancelledEvent = orderApprovalSaga.rollback(restaurantApprovalResponse);
        log.info("Publishing order cancelled event for order id: {} with failure messages: {}",
            restaurantApprovalResponse.getOrderId(), String.join(FAILURE_MESSAGE_DELIMITER, restaurantApprovalResponse.getFailureMessages()));
        orderCancelledEvent.fire();
    }
}
