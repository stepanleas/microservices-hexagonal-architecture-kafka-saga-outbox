package org.food.ordering.system.payment.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import org.food.ordering.system.domain.valueobject.PaymentStatus;
import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.payment.service.dataaccess.outbox.entity.OrderOutboxEntity;
import org.food.ordering.system.payment.service.dataaccess.outbox.repository.OrderOutboxJpaRepository;
import org.food.ordering.system.payment.service.domain.application.dto.PaymentRequest;
import org.food.ordering.system.payment.service.domain.application.ports.input.message.listener.PaymentRequestMessageListener;
import org.food.ordering.system.payment.service.container.PaymentServiceApplication;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.food.ordering.system.saga.order.SagaConstant.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(classes = PaymentServiceApplication.class)
public class PaymentRequestMessageListenerTest {

    @Autowired
    private PaymentRequestMessageListener paymentRequestMessageListener;

    @Autowired
    private OrderOutboxJpaRepository orderOutboxJpaRepository;

    private final static String CUSTOMER_ID = "d215b5f8-0249-4dc5-89a3-51fd148cfb41";
    private final static BigDecimal PRICE = new BigDecimal("100");

    @Test
    void testDoublePayment() {
        String sagaId = UUID.randomUUID().toString();
        try {
            paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
            paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
        } catch (DataAccessException e) {
            log.error("DataAccessException occurred with sql state: {}",
                ((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState());
        }

        assertOrderOutbox(sagaId);
    }

    @Test
    void testDoublePaymentWithThreads() {
        String sagaId = UUID.randomUUID().toString();

        ExecutorService executorService = Executors.newFixedThreadPool(2);;
        List<Callable<Object>> tasks = new ArrayList<>();

        tasks.add(Executors.callable(() -> {
            try {
                paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
            } catch (DataAccessException e) {
                log.error("DataAccessException occurred for thread1 with sql state: {}",
                    ((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState());
            }
        }));

        tasks.add(Executors.callable(() -> {
            try {
                paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
            } catch (DataAccessException e) {
                log.error("DataAccessException occurred for thread2 with sql state: {}",
                    ((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState());
            }
        }));

        try {
            executorService.invokeAll(tasks);
            assertOrderOutbox(sagaId);
        } catch (InterruptedException e) {
            log.error("Error calling complete payment!", e);
        } finally {
            executorService.shutdown();
        }
    }

    private void assertOrderOutbox(String sagaId) {
        Optional<OrderOutboxEntity> orderOutboxEntity = orderOutboxJpaRepository
            .findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(ORDER_SAGA_NAME,
                UUID.fromString(sagaId),
                PaymentStatus.COMPLETED,
                OutboxStatus.STARTED);

        assertTrue(orderOutboxEntity.isPresent());
        assertEquals(orderOutboxEntity.get().getSagaId().toString(), sagaId);
    }

    private PaymentRequest getPaymentRequest(String sagaId) {
        return PaymentRequest.builder()
            .id(UUID.randomUUID().toString())
            .sagaId(sagaId)
            .orderId(UUID.randomUUID().toString())
            .paymentOrderStatus(PaymentOrderStatus.PENDING)
            .customerId(CUSTOMER_ID)
            .price(PRICE)
            .createdAt(Instant.now())
            .build();
    }
}
