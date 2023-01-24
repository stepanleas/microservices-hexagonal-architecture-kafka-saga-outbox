package org.food.ordering.system.order.service.messaging.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import org.food.ordering.system.kafka.producer.KafkaMessageHelper;
import org.food.ordering.system.kafka.producer.service.KafkaProducer;
import org.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import org.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import org.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import org.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.food.ordering.system.outbox.OutboxStatus;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    @Override
    public void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
                        BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCall) {
        OrderPaymentEventPayload orderPaymentEventPayload = kafkaMessageHelper
            .getOrderPaymentEventPayload(orderPaymentOutboxMessage.getPayload(), OrderPaymentEventPayload.class);
        String sagaId = orderPaymentOutboxMessage.getSagaId().toString();

        log.info("Received {} for order id: {} and saga id: {}",
            OrderPaymentOutboxMessage.class.getSimpleName(),
            orderPaymentOutboxMessage.getId(),
            sagaId);

        try {
            PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper
                .orderPaymentEventToPaymentRequestAvroModel(sagaId, orderPaymentEventPayload);

            kafkaProducer.send(
                orderServiceConfigData.getPaymentRequestTopicName(),
                sagaId, paymentRequestAvroModel,
                kafkaMessageHelper.getKafkaCallback(orderServiceConfigData.getPaymentResponseTopicName(),
                    paymentRequestAvroModel,
                    orderPaymentOutboxMessage,
                    outboxCall,
                    orderPaymentEventPayload.getOrderId()));

            log.info("{} sent to kafka for order id: {} and saga id: {}" ,
                OrderPaymentEventPayload.class.getSimpleName(), orderPaymentEventPayload.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending {} to kafka with order id: {} and saga id: {}, error: {}",
                OrderPaymentEventPayload.class.getSimpleName(), orderPaymentEventPayload.getOrderId(), sagaId, e.getMessage());
        }
    }
}
