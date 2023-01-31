package org.food.ordering.system.payment.service.messaging.listener.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.kafka.consumer.KafkaConsumer;
import org.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import org.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import org.food.ordering.system.payment.service.domain.application.exception.PaymentApplicationServiceException;
import org.food.ordering.system.payment.service.domain.core.exception.PaymentNotFoundException;
import org.food.ordering.system.payment.service.domain.application.ports.input.message.listener.PaymentRequestMessageListener;
import org.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestKafkaListener implements KafkaConsumer<PaymentRequestAvroModel> {

    private final PaymentRequestMessageListener paymentRequestMessageListener;
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}", topics = "${payment-service.payment-request-topic-name}")
    public void receive(@Payload List<PaymentRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of payment requests received with keys: {}, partitions: {} and offsets: {}",
            messages.size(), keys, partitions, offsets);

        messages.forEach(paymentRequestAvroModel -> {
            try {
                if (PaymentOrderStatus.PENDING == paymentRequestAvroModel.getPaymentOrderStatus()) {
                    log.info("Processing payment for order id: {}", paymentRequestAvroModel.getOrderId());
                    paymentRequestMessageListener.completePayment(paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel));
                } else if (PaymentOrderStatus.CANCELLED == paymentRequestAvroModel.getPaymentOrderStatus()) {
                    log.info("Cancelling payment for order id: {}", paymentRequestAvroModel.getOrderId());
                    paymentRequestMessageListener.cancelPayment(paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel));
                }
            } catch (DataAccessException e) {
                SQLException sqlException = (SQLException) e.getRootCause();

                if (sqlException != null &&
                    sqlException.getSQLState() != null &&
                    PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                    // NO-OP for unique constraint exception
                    log.error("Caught unique constraint exception with sql state: {} in {} for order id: {}",
                        sqlException.getSQLState(),
                        this.getClass().getSimpleName(),
                        paymentRequestAvroModel.getOrderId());
                } else {
                    throw new PaymentApplicationServiceException("Throwing DataAccessException in " +
                        this.getClass().getSimpleName() + ": " + e.getMessage(), e);
                }
            } catch (PaymentNotFoundException e) {
                // NO-OP for PaymentNotFoundException
                log.error("No payment found for order id: {}", paymentRequestAvroModel.getOrderId());
            }
        });
    }
}
