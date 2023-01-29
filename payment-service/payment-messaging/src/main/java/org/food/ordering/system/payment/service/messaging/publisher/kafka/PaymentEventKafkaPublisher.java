package org.food.ordering.system.payment.service.messaging.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import org.food.ordering.system.kafka.producer.KafkaMessageHelper;
import org.food.ordering.system.kafka.producer.service.KafkaProducer;
import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import org.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import org.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import org.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import org.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventKafkaPublisher implements PaymentResponseMessagePublisher {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
    private final PaymentServiceConfigData paymentServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    @Override
    public void publish(OrderOutboxMessage orderOutboxMessage, BiConsumer<OrderOutboxMessage, OutboxStatus> outboxCallback) {
        OrderEventPayload orderEventPayload = kafkaMessageHelper.getOrderPaymentEventPayload(orderOutboxMessage.getPayload(), OrderEventPayload.class);
        String sagaId = orderOutboxMessage.getSagaId().toString();

        log.info("Received {} for order id: {} and saga id: {}",
            OrderOutboxMessage.class.getSimpleName(),
            orderEventPayload.getOrderId(),
            sagaId);

        try {
            PaymentResponseAvroModel paymentResponseAvroModel = paymentMessagingDataMapper
                .orderEventPayloadToPaymentResponseAvroModel(sagaId, orderEventPayload);
            kafkaProducer.send(paymentServiceConfigData.getPaymentResponseTopicName(),
                sagaId,
                paymentResponseAvroModel,
                kafkaMessageHelper.getKafkaCallback(paymentServiceConfigData.getPaymentResponseTopicName(),
                    paymentResponseAvroModel,
                    orderOutboxMessage,
                    outboxCallback,
                    orderEventPayload.getOrderId()));

            log.info("{} sent to kafka for order id: {} and saga id: {}" ,
                PaymentResponseAvroModel.class.getSimpleName(), paymentResponseAvroModel.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending {} to kafka with order id: {} and saga id: {}, error: {}",
                PaymentResponseAvroModel.class.getSimpleName(), orderEventPayload.getOrderId(), sagaId, e.getMessage());
        }
    }
}
