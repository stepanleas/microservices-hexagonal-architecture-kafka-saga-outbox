package org.food.ordering.system.payment.service.domain.application.ports.input.message.listener.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.payment.service.domain.application.PaymentRequestHelper;
import org.food.ordering.system.payment.service.domain.application.dto.PaymentRequest;
import org.food.ordering.system.payment.service.domain.application.ports.input.message.listener.PaymentRequestMessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestMessageListenerImpl implements PaymentRequestMessageListener {

    private final PaymentRequestHelper paymentRequestHelper;

    @Override
    public void completePayment(PaymentRequest paymentRequest) {
        paymentRequestHelper.persistPayment(paymentRequest);
    }

    @Override
    public void cancelPayment(PaymentRequest paymentRequest) {
        paymentRequestHelper.persistCancelPayment(paymentRequest);
    }
}
