package org.food.ordering.system.restaurant.service.domain.application.ports.input.message.listener.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.restaurant.service.domain.application.RestaurantApprovalRequestHelper;
import org.food.ordering.system.restaurant.service.domain.application.dto.RestaurantApprovalRequest;
import org.food.ordering.system.restaurant.service.domain.application.ports.input.message.listener.RestaurantApprovalRequestMessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantApprovalRequestMessageListenerImpl implements RestaurantApprovalRequestMessageListener {

    private final RestaurantApprovalRequestHelper restaurantApprovalRequestHelper;

    @Override
    public void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest) {
        restaurantApprovalRequestHelper.persistOrderApproval(restaurantApprovalRequest);
    }
}
