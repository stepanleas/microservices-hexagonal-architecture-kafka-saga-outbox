package org.food.ordering.system.order.service.messaging.listener.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.kafka.consumer.KafkaConsumer;
import org.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import org.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import org.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import org.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseAvroModel> {

    private final PaymentResponseMessageListener paymentResponseMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}", topics = "${order-service.payment-response-topic-name}")
    public void receive(@Payload List<PaymentResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of payment responses received with keys: {}, partitions: {} and offsets: {}",
            messages.size(), keys, partitions, offsets);

        messages.forEach(paymentRequestAvroModel -> {
            if (PaymentStatus.COMPLETED == paymentRequestAvroModel.getPaymentStatus()) {
                log.info("Processing successful payment for order id: {}", paymentRequestAvroModel.getOrderId());
                paymentResponseMessageListener.paymentCompleted(orderMessagingDataMapper
                    .paymentResponseAvroModelToPaymentResponse(paymentRequestAvroModel));
            } else if (PaymentStatus.CANCELLED == paymentRequestAvroModel.getPaymentStatus() || PaymentStatus.FAILED == paymentRequestAvroModel.getPaymentStatus()) {
                log.info("Processing unsuccessful payment for order id: {}", paymentRequestAvroModel.getOrderId());
                paymentResponseMessageListener.paymentCancelled(orderMessagingDataMapper
                    .paymentResponseAvroModelToPaymentResponse(paymentRequestAvroModel));
            }
        });
    }
}
