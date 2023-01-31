package org.food.ordering.system.payment.service.dataaccess.payment.adapter;

import lombok.RequiredArgsConstructor;
import org.food.ordering.system.payment.service.dataaccess.payment.mapper.PaymentDataAccessMapper;
import org.food.ordering.system.payment.service.dataaccess.payment.repository.PaymentJpaRepository;
import org.food.ordering.system.payment.service.domain.core.entity.Payment;
import org.food.ordering.system.payment.service.domain.application.ports.output.repository.PaymentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentDataAccessMapper paymentDataAccessMapper;

    @Override
    public Payment save(Payment payment) {
        return paymentDataAccessMapper
                .paymentEntityToPayment(paymentJpaRepository
                        .save(paymentDataAccessMapper.paymentToPaymentEntity(payment)));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId)
                .map(paymentDataAccessMapper::paymentEntityToPayment);
    }
}
