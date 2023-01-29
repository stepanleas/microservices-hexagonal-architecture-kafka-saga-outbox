package org.food.ordering.system.payment.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.domain.valueobject.CustomerId;
import org.food.ordering.system.domain.valueobject.PaymentStatus;
import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import org.food.ordering.system.payment.service.domain.entity.CreditEntry;
import org.food.ordering.system.payment.service.domain.entity.CreditHistory;
import org.food.ordering.system.payment.service.domain.entity.Payment;
import org.food.ordering.system.payment.service.domain.event.PaymentEvent;
import org.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import org.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import org.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import org.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import org.food.ordering.system.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import org.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import org.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import org.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import org.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestHelper {
    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final OrderOutboxHelper orderOutboxHelper;
    private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;

    @Transactional
    public void persistPayment(PaymentRequest paymentRequest) {
        if (publishIfOutboxMessageProcessedForPayment(paymentRequest,PaymentStatus.COMPLETED)) {
            log.info("Outbox Message with sagaId : {} already save !", paymentRequest.getSagaId());
            return;
        }

        log.info("Received payment complete event for order id: {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistoryList = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistoryList, failureMessages);
        persistDbObjects(payment, creditEntry, creditHistoryList, failureMessages);

        orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
            paymentEvent.getPayment().getPaymentStatus(),
            OutboxStatus.STARTED,
            UUID.fromString(paymentRequest.getSagaId()));
    }

    private void persistDbObjects(Payment payment, CreditEntry creditEntry, List<CreditHistory> creditHistoryList, List<String> failureMessages) {
        paymentRepository.save(payment);
        if (failureMessages.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(creditHistoryList.get(creditHistoryList.size() - 1));
        }
    }

    @Transactional
    public void persistCancelPayment(PaymentRequest paymentRequest) {
        if (publishIfOutboxMessageProcessedForPayment(paymentRequest,PaymentStatus.CANCELLED)) {
            log.info("Outbox Message with sagaId : {} already save !", paymentRequest.getSagaId());
            return;
        }

        log.info("Received payment rollback event for order id: {}", paymentRequest.getOrderId());
        Optional<Payment> paymentResponse = paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));
        if (paymentResponse.isEmpty()) {
            log.error("Payment with order id: {} could not be found!", paymentRequest.getOrderId());
            throw new PaymentNotFoundException("Payment with order id: "+ paymentRequest.getOrderId() +" could not be found!");
        }

        Payment payment = paymentResponse.get();
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistoryList = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = paymentDomainService.validateAndCancelPayment(payment, creditEntry, creditHistoryList, failureMessages);
        persistDbObjects(payment, creditEntry, creditHistoryList, failureMessages);

        orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
            paymentEvent.getPayment().getPaymentStatus(),
            OutboxStatus.STARTED,
            UUID.fromString(paymentRequest.getSagaId()));
    }

    private List<CreditHistory> getCreditHistory(CustomerId customerId) {
        Optional<List<CreditHistory>> creditHistoryList = creditHistoryRepository.findByCustomerId(customerId);

        if (creditHistoryList.isEmpty()) {
            log.error("Could not find credit history for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit history for customer: " + customerId.getValue());
        }

        return creditHistoryList.get();
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        Optional<CreditEntry> creditEntry = creditEntryRepository.findByCustomerId(customerId);
        if (creditEntry.isEmpty()) {
            log.error("Could not find credit entry for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit entry for customer: " + customerId.getValue());
        }

        return creditEntry.get();
    }

    private boolean publishIfOutboxMessageProcessedForPayment(PaymentRequest paymentRequest, PaymentStatus paymentStatus) {
        Optional<OrderOutboxMessage> outboxMessage = orderOutboxHelper
            .getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(UUID.fromString(paymentRequest.getSagaId()), paymentStatus);
        if (outboxMessage.isPresent()) {
            paymentResponseMessagePublisher.publish(outboxMessage.get(), orderOutboxHelper::updateOutboxMessage);
            return true;
        }

        return false;
    }
}
