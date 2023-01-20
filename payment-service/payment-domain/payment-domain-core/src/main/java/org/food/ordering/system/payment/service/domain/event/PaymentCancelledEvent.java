package org.food.ordering.system.payment.service.domain.event;

import org.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import org.food.ordering.system.payment.service.domain.entity.Payment;

import java.time.ZonedDateTime;
import java.util.Collections;

public class PaymentCancelledEvent extends PaymentEvent {

    private DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher;

    public PaymentCancelledEvent(Payment payment, ZonedDateTime createdAt, DomainEventPublisher<PaymentCancelledEvent> domainEventPublisher) {
        super(payment, createdAt, Collections.emptyList());
        this.paymentCancelledEventDomainEventPublisher = domainEventPublisher;
    }

    @Override
    public void fire() {
        paymentCancelledEventDomainEventPublisher.publish(this);
    }
}
