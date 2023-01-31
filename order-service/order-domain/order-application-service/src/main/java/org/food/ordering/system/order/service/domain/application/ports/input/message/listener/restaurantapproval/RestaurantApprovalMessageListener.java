package org.food.ordering.system.order.service.domain.application.ports.input.message.listener.restaurantapproval;

import org.food.ordering.system.order.service.domain.application.dto.message.RestaurantApprovalResponse;

public interface RestaurantApprovalMessageListener {
    void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse);
    void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);
}
