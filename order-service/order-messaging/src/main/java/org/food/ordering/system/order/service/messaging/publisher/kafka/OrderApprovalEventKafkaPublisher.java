package org.food.ordering.system.order.service.messaging.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import org.food.ordering.system.kafka.producer.KafkaMessageHelper;
import org.food.ordering.system.kafka.producer.service.KafkaProducer;
import org.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import org.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import org.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import org.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.food.ordering.system.outbox.OutboxStatus;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalEventKafkaPublisher implements RestaurantApprovalRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    @Override
    public void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage, BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCall) {
        OrderApprovalEventPayload orderApprovalEventPayload = kafkaMessageHelper
            .getOrderPaymentEventPayload(orderApprovalOutboxMessage.getPayload(), OrderApprovalEventPayload.class);
        String sagaId = orderApprovalOutboxMessage.getSagaId().toString();

        log.info("Received {} for order id: {} and saga id: {}",
            OrderPaymentOutboxMessage.class.getSimpleName(),
            orderApprovalOutboxMessage.getId(),
            sagaId);

        try {
            RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper
                .orderApprovalEventToRestaurantApprovalRequestAvroModel(sagaId, orderApprovalEventPayload);

            kafkaProducer.send(
                orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                sagaId, restaurantApprovalRequestAvroModel,
                kafkaMessageHelper.getKafkaCallback(orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                    restaurantApprovalRequestAvroModel,
                    orderApprovalOutboxMessage,
                    outboxCall,
                    orderApprovalEventPayload.getOrderId()));

            log.info("{} sent to kafka for order id: {} and saga id: {}" ,
                OrderApprovalEventPayload.class.getSimpleName(), orderApprovalEventPayload.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending {} to kafka with order id: {} and saga id: {}, error: {}",
                OrderApprovalEventPayload.class.getSimpleName(), orderApprovalEventPayload.getOrderId(), sagaId, e.getMessage());
        }
    }
}
