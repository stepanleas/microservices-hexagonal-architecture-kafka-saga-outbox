package org.food.ordering.system.order.service.domain.application.mapper;

import org.food.ordering.system.domain.valueobject.*;
import org.food.ordering.system.order.service.domain.application.dto.create.CreateOrderCommand;
import org.food.ordering.system.order.service.domain.application.dto.create.CreateOrderResponse;
import org.food.ordering.system.order.service.domain.application.dto.create.OrderAddress;
import org.food.ordering.system.order.service.domain.application.dto.message.CustomerModel;
import org.food.ordering.system.order.service.domain.application.dto.track.TrackOrderResponse;
import org.food.ordering.system.order.service.domain.application.outbox.model.approval.OrderApprovalEventPayload;
import org.food.ordering.system.order.service.domain.application.outbox.model.approval.OrderApprovalEventProduct;
import org.food.ordering.system.order.service.domain.application.outbox.model.payment.OrderPaymentEventPayload;
import org.food.ordering.system.order.service.domain.core.entity.*;
import org.food.ordering.system.order.service.domain.core.event.OrderCancelledEvent;
import org.food.ordering.system.order.service.domain.core.event.OrderCreatedEvent;
import org.food.ordering.system.order.service.domain.core.event.OrderPaidEvent;
import org.food.ordering.system.order.service.domain.core.valueobject.StreetAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderDataMapper {

    public Customer customerModelToCustomer(CustomerModel customerModel) {
        return new Customer(new CustomerId(UUID.fromString(customerModel.getId())),
            customerModel.getUsername(),
            customerModel.getFirstName(),
            customerModel.getLastName());
    }

    public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
        return Restaurant.builder()
            .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
            .products(createOrderCommand.getItems()
                    .stream()
                    .map(orderItem -> new Product(new ProductId(orderItem.getProductId())))
                    .toList())
            .build();
    }

    public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
        return Order.builder()
            .customerId(new CustomerId(createOrderCommand.getCustomerId()))
            .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
            .deliveryAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
            .price(new Money(createOrderCommand.getPrice()))
            .items(orderItemsToOrderItemEntities(createOrderCommand.getItems()))
            .build();
    }

    public CreateOrderResponse orderToCreateOrderResponse(Order order, String message) {
        return CreateOrderResponse.builder()
            .orderTrackingId(order.getTrackingId().getValue())
            .orderStatus(order.getOrderStatus())
            .message(message)
            .build();
    }

    public TrackOrderResponse orderToTrackOrderResponse(Order order) {
        return TrackOrderResponse.builder()
            .orderTrackingId(order.getTrackingId().getValue())
            .orderStatus(order.getOrderStatus())
            .failureMessages(order.getFailureMessages())
            .build();
    }

    public OrderPaymentEventPayload orderCreatedEventToOrderPaymentEventPayload(OrderCreatedEvent orderCreatedEvent) {
        return OrderPaymentEventPayload.builder()
            .customerId(orderCreatedEvent.getOrder().getCustomerId().getValue().toString())
            .orderId(orderCreatedEvent.getOrder().getId().getValue().toString())
            .price(orderCreatedEvent.getOrder().getPrice().getAmount())
            .createdAt(orderCreatedEvent.getCreatedAt())
            .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
            .build();
    }

    public OrderPaymentEventPayload orderCancelledEventToOrderPaymentEventPayload(OrderCancelledEvent orderCancelledEvent) {
        return OrderPaymentEventPayload.builder()
            .customerId(orderCancelledEvent.getOrder().getCustomerId().getValue().toString())
            .orderId(orderCancelledEvent.getOrder().getId().getValue().toString())
            .price(orderCancelledEvent.getOrder().getPrice().getAmount())
            .createdAt(orderCancelledEvent.getCreatedAt())
            .paymentOrderStatus(PaymentOrderStatus.CANCELLED.name())
            .build();
    }

    public OrderApprovalEventPayload orderPaidEventToOrderApprovalEventPayload(OrderPaidEvent orderPaidEvent) {
        return OrderApprovalEventPayload.builder()
            .orderId(orderPaidEvent.getOrder().getId().getValue().toString())
            .restaurantId(orderPaidEvent.getOrder().getRestaurantId().getValue().toString())
            .restaurantOrderStatus(RestaurantOrderStatus.PAID.name())
            .products(orderPaidEvent.getOrder().getItems().stream().map(orderItem ->
                OrderApprovalEventProduct.builder()
                    .id(orderItem.getProduct().getId().getValue().toString())
                    .quantity(orderItem.getQuantity())
                    .build()).toList())
            .price(orderPaidEvent.getOrder().getPrice().getAmount())
            .createdAt(orderPaidEvent.getCreatedAt())
            .build();
    }

    private List<OrderItem> orderItemsToOrderItemEntities(List<org.food.ordering.system.order.service.domain.application.dto.create.OrderItem> orderItems) {
        return orderItems.stream()
            .map(orderItem -> OrderItem.builder()
                .product(new Product(new ProductId(orderItem.getProductId())))
                .price(new Money(orderItem.getPrice()))
                .quantity(orderItem.getQuantity())
                .subTotal(new Money(orderItem.getSubTotal()))
                .build())
            .toList();
    }

    private StreetAddress orderAddressToStreetAddress(OrderAddress orderAddress) {
        return new StreetAddress(UUID.randomUUID(), orderAddress.getStreet(), orderAddress.getPostalCode(), orderAddress.getCity());
    }
}