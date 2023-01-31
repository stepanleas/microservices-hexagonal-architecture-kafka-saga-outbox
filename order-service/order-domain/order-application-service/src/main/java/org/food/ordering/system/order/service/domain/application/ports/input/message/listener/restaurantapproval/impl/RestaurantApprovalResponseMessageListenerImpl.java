package org.food.ordering.system.order.service.domain.application.ports.input.message.listener.restaurantapproval.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.application.dto.message.RestaurantApprovalResponse;
import org.food.ordering.system.order.service.domain.application.saga.OrderApprovalSaga;
import org.food.ordering.system.order.service.domain.application.ports.input.message.listener.restaurantapproval.RestaurantApprovalMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static org.food.ordering.system.order.service.domain.core.entity.Order.FAILURE_MESSAGE_DELIMITER;

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
        orderApprovalSaga.rollback(restaurantApprovalResponse);
        log.info("{} order rollback operation is completed for order id: {} with failure messages: {}",
            OrderApprovalSaga.class.getSimpleName(),
            restaurantApprovalResponse.getOrderId(),
            String.join(FAILURE_MESSAGE_DELIMITER, restaurantApprovalResponse.getFailureMessages()));
    }
}
