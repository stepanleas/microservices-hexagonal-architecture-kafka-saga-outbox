package org.food.ordering.system.restaurant.service.messaging.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import org.food.ordering.system.kafka.producer.KafkaMessageHelper;
import org.food.ordering.system.kafka.producer.service.KafkaProducer;
import org.food.ordering.system.restaurant.service.domain.config.RestaurantServiceConfigData;
import org.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import org.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderApprovedMessagePublisher;
import org.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovedKafkaMessagePublisher implements OrderApprovedMessagePublisher {

    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
    private final RestaurantServiceConfigData restaurantServiceConfigData;
    private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    @Override
    public void publish(OrderApprovedEvent domainEvent) {
        String orderId = domainEvent.getOrderApproval().getOrderId().getValue().toString();
        log.info("Received {} for order id: {}", domainEvent.getClass().getSimpleName(), orderId);

        try {
            RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel = restaurantMessagingDataMapper
                .orderApprovedEventToRestaurantApprovalResponseAvroModel(domainEvent);

            kafkaProducer.send(
                restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
                orderId, restaurantApprovalResponseAvroModel,
                kafkaMessageHelper.getKafkaCallback(restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),restaurantApprovalResponseAvroModel, orderId));

            log.info("{} sent to Kafka for order id: {}", restaurantApprovalResponseAvroModel.getClass().getSimpleName(), restaurantApprovalResponseAvroModel.getOrderId());
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalResponseAvroModel message to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}
