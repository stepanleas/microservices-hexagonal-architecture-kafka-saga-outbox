package org.food.ordering.system.payment.service.domain.application.ports.output.repository;

import org.food.ordering.system.payment.service.domain.core.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByOrderId(UUID id);
}
