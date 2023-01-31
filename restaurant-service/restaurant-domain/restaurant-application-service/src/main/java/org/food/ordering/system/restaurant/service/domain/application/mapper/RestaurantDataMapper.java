package org.food.ordering.system.restaurant.service.domain.application.mapper;

import org.food.ordering.system.domain.valueobject.Money;
import org.food.ordering.system.domain.valueobject.OrderId;
import org.food.ordering.system.domain.valueobject.OrderStatus;
import org.food.ordering.system.domain.valueobject.RestaurantId;
import org.food.ordering.system.restaurant.service.domain.application.dto.RestaurantApprovalRequest;
import org.food.ordering.system.restaurant.service.domain.application.outbox.model.OrderEventPayload;
import org.food.ordering.system.restaurant.service.domain.core.entity.OrderDetail;
import org.food.ordering.system.restaurant.service.domain.core.entity.Product;
import org.food.ordering.system.restaurant.service.domain.core.entity.Restaurant;
import org.food.ordering.system.restaurant.service.domain.core.event.OrderApprovalEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RestaurantDataMapper {

    public Restaurant restaurantApprovalRequestToRestaurant(RestaurantApprovalRequest request) {
        return Restaurant.builder()
            .restaurantId(new RestaurantId(UUID.fromString(request.getRestaurantId())))
            .orderDetail(OrderDetail.builder()
                .orderId(new OrderId(UUID.fromString(request.getOrderId())))
                .products(request.getProducts().stream().map(
                    product -> Product.builder()
                        .productId(product.getId())
                        .quantity(product.getQuantity())
                        .build()
                ).toList())
                .totalAmount(new Money(request.getPrice()))
                .orderStatus(OrderStatus.valueOf(request.getRestaurantOrderStatus().name()))
                .build())
            .build();
    }

    public OrderEventPayload orderApprovalEventToOrderEventPayload(OrderApprovalEvent orderApprovalEvent) {
        return OrderEventPayload.builder()
            .orderId(orderApprovalEvent.getOrderApproval().getOrderId().getValue().toString())
            .restaurantId(orderApprovalEvent.getRestaurantId().getValue().toString())
            .orderApprovalStatus(orderApprovalEvent.getOrderApproval().getApprovalStatus().name())
            .createdAt(orderApprovalEvent.getCreatedAt())
            .failureMessages(orderApprovalEvent.getFailureMessages())
            .build();
    }
}
