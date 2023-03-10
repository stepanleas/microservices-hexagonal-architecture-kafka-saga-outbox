package org.food.ordering.system.order.service.domain.core;

import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.core.entity.Order;
import org.food.ordering.system.order.service.domain.core.entity.OrderItem;
import org.food.ordering.system.order.service.domain.core.entity.Product;
import org.food.ordering.system.order.service.domain.core.entity.Restaurant;
import org.food.ordering.system.order.service.domain.core.event.OrderCancelledEvent;
import org.food.ordering.system.order.service.domain.core.event.OrderCreatedEvent;
import org.food.ordering.system.order.service.domain.core.event.OrderPaidEvent;
import org.food.ordering.system.order.service.domain.core.exception.OrderDomainException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.food.ordering.system.domain.DomainConstants.UTC;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

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
        Map<UUID, List<Product>> orderProducts = order.getItems()
            .stream()
            .map(OrderItem::getProduct)
            .collect(Collectors.groupingBy(
                product -> product.getId().getValue(),
                Collectors.mapping(product -> product, Collectors.toList())));

        restaurant.getProducts()
            .stream()
            .filter(restaurantProduct -> orderProducts.get(restaurantProduct.getId().getValue()) != null)
            .forEach(restaurantProduct -> {
                orderProducts.get(restaurantProduct.getId().getValue())
                    .stream()
                    .filter(orderProduct -> orderProduct.equals(restaurantProduct))
                    .forEach(orderProduct -> {
                        orderProduct.updateWithConfirmedNameAndPrice(restaurantProduct.getName(), restaurantProduct.getPrice());
                    });
            });
    }
}
