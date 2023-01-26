package org.food.ordering.system.payment.service.messaging.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import org.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import org.food.ordering.system.kafka.producer.KafkaMessageHelper;
import org.food.ordering.system.kafka.producer.service.KafkaProducer;
import org.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import org.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import org.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFailedKafkaMessagePublisher implements DomainEventPublisher<PaymentFailedEvent> {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
    private final PaymentServiceConfigData paymentServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    @Override
    public void publish(PaymentFailedEvent domainEvent) {
        String orderId = domainEvent.getPayment().getOrderId().getValue().toString();
        log.info("Received {} for order id: {}", domainEvent.getClass().getSimpleName(), orderId);

        try {
            PaymentResponseAvroModel paymentResponseAvroModel = paymentMessagingDataMapper.paymentFailedEventToPaymentResponseAvroModel(domainEvent);

            kafkaProducer.send(paymentServiceConfigData.getPaymentResponseTopicName(),
                orderId,
                paymentResponseAvroModel,
                kafkaMessageHelper.getKafkaCallback(paymentServiceConfigData.getPaymentResponseTopicName(),
                    paymentResponseAvroModel,
                    orderId));

            log.info("{} sent to kafka for order id: {}", paymentResponseAvroModel.getClass().getSimpleName(), orderId);
        } catch (Exception e) {
            log.error("Error while sending PaymentResponseAvroModel message to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}
