package org.food.ordering.system.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.food.ordering.system.order.service.domain.core.exception.OrderDomainException;
import org.food.ordering.system.outbox.OutboxStatus;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageHelper {

    private final ObjectMapper objectMapper;

    public <T> T getOrderPaymentEventPayload(String payload, Class<T> outputType) {
        try {
            return objectMapper.readValue(payload, outputType);
        } catch (JsonProcessingException e) {
            log.error("Could not read {} object!", outputType.getName());
            throw new OrderDomainException("Could not read "+ outputType.getName() +" object!", e);
        }
    }

    public <T, U> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(String responseTopicName,
                                                                                   T avroModel,
                                                                                   U outboxMessage,
                                                                                   BiConsumer<U, OutboxStatus> outboxCall,
                                                                                   String orderId) {
        // TODO: replace with completable future
        return new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Error while sending {} with message: {} and outbox type: {} to topic: {}",
                    avroModel.getClass().getSimpleName(), outboxMessage.getClass().getSimpleName(),
                    avroModel, responseTopicName, ex);
                outboxCall.accept(outboxMessage, OutboxStatus.FAILED);
            }

            @Override
            public void onSuccess(SendResult<String, T> result) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Received successful response from Kafka for order id: {}. Topic: {}, Partition: {}, Offset: {}, Timestamp: {}",
                    orderId,
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    metadata.timestamp());
                outboxCall.accept(outboxMessage, OutboxStatus.COMPLETED);
            }
        };
    }
}
