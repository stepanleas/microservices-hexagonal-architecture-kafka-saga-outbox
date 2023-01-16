package org.food.ordering.system.payment.service.domain.event;

import org.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import org.food.ordering.system.payment.service.domain.entity.Payment;

import java.time.ZonedDateTime;
import java.util.List;

public class PaymentFailedEvent extends PaymentEvent {

    private final DomainEventPublisher<PaymentFailedEvent> domainEventPublisher;
    public PaymentFailedEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMessages, DomainEventPublisher<PaymentFailedEvent> domainEventPublisher) {
        super(payment, createdAt, failureMessages);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void fire() {
        domainEventPublisher.publish(this);
    }
}
