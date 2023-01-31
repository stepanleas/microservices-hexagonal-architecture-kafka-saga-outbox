package org.food.ordering.system.order.service.domain.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.application.dto.create.CreateOrderCommand;
import org.food.ordering.system.order.service.domain.application.dto.create.CreateOrderResponse;
import org.food.ordering.system.order.service.domain.application.mapper.OrderDataMapper;
import org.food.ordering.system.order.service.domain.application.outbox.scheduler.payment.PaymentOutboxHelper;
import org.food.ordering.system.order.service.domain.application.saga.OrderSagaHelper;
import org.food.ordering.system.order.service.domain.core.event.OrderCreatedEvent;
import org.food.ordering.system.outbox.OutboxStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateCommandHandler {
    private final OrderCreateHelper orderCreateHelper;
    private final OrderDataMapper orderDataMapper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final OrderSagaHelper orderSagaHelper;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
        log.info("Order is created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
        CreateOrderResponse createOrderResponse = orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(), "Order Created Successfully");

        paymentOutboxHelper.savePaymentOutboxMessage(
            orderDataMapper.orderCreatedEventToOrderPaymentEventPayload(orderCreatedEvent),
            orderCreatedEvent.getOrder().getOrderStatus(),
            orderSagaHelper.orderStatusToSagaStatus(orderCreatedEvent.getOrder().getOrderStatus()),
            OutboxStatus.STARTED,
            UUID.randomUUID());

        log.info("Returning {} with order id: {}",
            CreateOrderResponse.class.getSimpleName(), orderCreatedEvent.getOrder().getId());

        return createOrderResponse;
    }
}
