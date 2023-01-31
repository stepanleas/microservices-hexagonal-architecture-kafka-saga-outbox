package org.food.ordering.system.restaurant.service.domain.application.ports.input.message.listener;

import org.food.ordering.system.restaurant.service.domain.application.dto.RestaurantApprovalRequest;

public interface RestaurantApprovalRequestMessageListener {
    void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest);
}
