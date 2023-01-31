package org.food.ordering.system.restaurant.service.domain.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.domain.valueobject.OrderId;
import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.restaurant.service.domain.core.RestaurantDomainService;
import org.food.ordering.system.restaurant.service.domain.application.dto.RestaurantApprovalRequest;
import org.food.ordering.system.restaurant.service.domain.application.mapper.RestaurantDataMapper;
import org.food.ordering.system.restaurant.service.domain.application.outbox.model.OrderOutboxMessage;
import org.food.ordering.system.restaurant.service.domain.application.outbox.scheduler.OrderOutboxHelper;
import org.food.ordering.system.restaurant.service.domain.application.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
import org.food.ordering.system.restaurant.service.domain.application.ports.output.repository.OrderApprovalRepository;
import org.food.ordering.system.restaurant.service.domain.application.ports.output.repository.RestaurantRepository;
import org.food.ordering.system.restaurant.service.domain.core.entity.Restaurant;
import org.food.ordering.system.restaurant.service.domain.core.event.OrderApprovalEvent;
import org.food.ordering.system.restaurant.service.domain.core.exception.RestaurantNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalRequestHelper {
    private final RestaurantDomainService restaurantDomainService;
    private final RestaurantDataMapper restaurantDataMapper;
    private final RestaurantRepository restaurantRepository;
    private final OrderApprovalRepository orderApprovalRepository;
    private final OrderOutboxHelper orderOutboxHelper;
    private final RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher;

    @Transactional
    public void persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        if (publishIfOutboxMessageProcessed(restaurantApprovalRequest)) {
            log.info("An outbox message with saga id: {} already saved to database!",
                restaurantApprovalRequest.getSagaId());
            return;
        }

        log.info("Processing restaurant approval for order id: {}", restaurantApprovalRequest.getOrderId());
        List<String> failureMessages = new ArrayList<>();
        Restaurant restaurant = findRestaurant(restaurantApprovalRequest);
        OrderApprovalEvent orderApprovalEvent =
            restaurantDomainService.validateOrder(
                restaurant,
                failureMessages);
        orderApprovalRepository.save(restaurant.getOrderApproval());

        orderOutboxHelper
            .saveOrderOutboxMessage(restaurantDataMapper.
                    orderApprovalEventToOrderEventPayload(orderApprovalEvent),
                orderApprovalEvent.getOrderApproval().getApprovalStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(restaurantApprovalRequest.getSagaId()));

    }

    private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        Restaurant restaurant = restaurantDataMapper
            .restaurantApprovalRequestToRestaurant(restaurantApprovalRequest);
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantInformation(restaurant);
        if (restaurantResult.isEmpty()) {
            log.error("Restaurant with id " + restaurant.getId().getValue() + " not found!");
            throw new RestaurantNotFoundException("Restaurant with id " + restaurant.getId().getValue() +
                " not found!");
        }

        Restaurant restaurantEntity = restaurantResult.get();
        restaurant.setActive(restaurantEntity.isActive());
        restaurant.getOrderDetail().getProducts().forEach(product ->
            restaurantEntity.getOrderDetail().getProducts().forEach(p -> {
                if (p.getId().equals(product.getId())) {
                    product.updateWithConfirmedPriceAndAvailability(p.getName(), p.getPrice(), p.isAvailable());
                }
            }));
        restaurant.getOrderDetail().setId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())));

        return restaurant;
    }

    private boolean publishIfOutboxMessageProcessed(RestaurantApprovalRequest restaurantApprovalRequest) {
        Optional<OrderOutboxMessage> orderOutboxMessage =
            orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndOutboxStatus(UUID
                .fromString(restaurantApprovalRequest.getSagaId()), OutboxStatus.COMPLETED);
        if (orderOutboxMessage.isPresent()) {
            restaurantApprovalResponseMessagePublisher.publish(orderOutboxMessage.get(),
                orderOutboxHelper::updateOutboxStatus);
            return true;
        }
        return false;
    }
}
