package org.food.ordering.system.order.service.domain.ports.input.message.listener.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.order.service.domain.saga.OrderPaymentSaga;
import org.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import org.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static org.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

    private final OrderPaymentSaga orderPaymentSaga;

    @Override
    public void paymentCompleted(PaymentResponse paymentResponse) {
        orderPaymentSaga.process(paymentResponse);
        log.info("{} process operation is completed for order id: {}",
            OrderPaymentSaga.class.getSimpleName(), paymentResponse.getOrderId());
    }

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {
        orderPaymentSaga.rollback(paymentResponse);
        log.info("Order is roll backed for order id : {} with failure messages: {}",
            paymentResponse.getOrderId(),
            String.join(FAILURE_MESSAGE_DELIMITER, paymentResponse.getFailureMessages()));
    }
}
