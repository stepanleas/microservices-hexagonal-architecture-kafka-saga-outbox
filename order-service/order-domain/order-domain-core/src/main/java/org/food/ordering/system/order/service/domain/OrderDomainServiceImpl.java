package org.food.ordering.system.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.domain.valueobject.ProductId;
import org.food.ordering.system.order.service.domain.entity.Order;
import org.food.ordering.system.order.service.domain.entity.OrderItem;
import org.food.ordering.system.order.service.domain.entity.Product;
import org.food.ordering.system.order.service.domain.entity.Restaurant;
import org.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import org.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import org.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import org.food.ordering.system.order.service.domain.exception.OrderDomainException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

    private static final String UTC = "UTC";
    
    @Override
    public OrderCreatedEvent validatedAndInitiateOrder(Order order, Restaurant restaurant) {
        validateRestaurant(restaurant);
        setOrderProductInformation(order, restaurant);
        order.validateOrder();
        order.initializeOrder();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        order.pay();
        log.info("Order with id: {} is paid", order.getId().getValue());
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {
        order.initCancel(failureMessages);
        log.info("Order payment is cancelling for order id: {}", order.getId().getValue());
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order with id: {} is cancelled", order.getId().getValue());
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue()
                + " is currently not active!");
        }
    }

    private void setOrderProductInformation(Order order, Restaurant restaurant) {
        Map<ProductId, Product> orderProducts = order.getItems()
            .stream()
            .map(OrderItem::getProduct)
            .collect(Collectors.toMap(Product::getId, product -> product));

        restaurant.getProducts()
            .stream()
            .filter(restaurantProduct ->
                orderProducts.get(restaurantProduct.getId()) != null
                && orderProducts.get(restaurantProduct.getId()).equals(restaurantProduct))
            .forEach(restaurantProduct -> {
                orderProducts.get(restaurantProduct.getId())
                    .updateWithConfirmedNameAndPrice(restaurantProduct.getName(), restaurantProduct.getPrice());
            });
    }
}
