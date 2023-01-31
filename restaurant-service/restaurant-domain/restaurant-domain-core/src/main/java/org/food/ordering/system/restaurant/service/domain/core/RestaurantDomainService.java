package org.food.ordering.system.restaurant.service.domain.core;

import org.food.ordering.system.restaurant.service.domain.core.entity.Restaurant;
import org.food.ordering.system.restaurant.service.domain.core.event.OrderApprovalEvent;

import java.util.List;

public interface RestaurantDomainService {
    OrderApprovalEvent validateOrder(Restaurant restaurant,
                                     List<String> failureMessages);
}
