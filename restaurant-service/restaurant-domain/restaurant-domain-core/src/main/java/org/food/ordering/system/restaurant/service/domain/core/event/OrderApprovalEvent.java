package org.food.ordering.system.restaurant.service.domain.core.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.food.ordering.system.domain.events.DomainEvent;
import org.food.ordering.system.domain.valueobject.RestaurantId;
import org.food.ordering.system.restaurant.service.domain.core.entity.OrderApproval;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class OrderApprovalEvent implements DomainEvent<OrderApproval> {
    private final OrderApproval orderApproval;
    private final RestaurantId restaurantId;
    private final List<String> failureMessages;
    private final ZonedDateTime createdAt;
}
