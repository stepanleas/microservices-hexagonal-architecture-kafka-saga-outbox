package org.food.ordering.system.payment.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.domain.events.publisher.DomainEventPublisher;
import org.food.ordering.system.domain.valueobject.Money;
import org.food.ordering.system.domain.valueobject.PaymentStatus;
import org.food.ordering.system.payment.service.domain.entity.CreditEntry;
import org.food.ordering.system.payment.service.domain.entity.CreditHistory;
import org.food.ordering.system.payment.service.domain.entity.Payment;
import org.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import org.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import org.food.ordering.system.payment.service.domain.event.PaymentEvent;
import org.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import org.food.ordering.system.payment.service.domain.valueobject.CreditHistoryId;
import org.food.ordering.system.payment.service.domain.valueobject.TransactionType;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.food.ordering.system.domain.DomainConstants.UTC;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {
    @Override
    public PaymentEvent validateAndInitiatePayment(Payment payment,
                                                   CreditEntry creditEntry,
                                                   List<CreditHistory> creditHistoryList,
                                                   List<String> failureMessages) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistoryList, TransactionType.DEBIT);
        validateCreditHistory(creditEntry, creditHistoryList, failureMessages);

        if (failureMessages.isEmpty()) {
            log.info("Payment is initiated for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.COMPLETED);
            return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)));
        }

        log.info("Payment initiation is failed for order id: {}", payment.getOrderId().getValue());
        payment.updateStatus(PaymentStatus.FAILED);
        return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages);
    }

    @Override
    public PaymentEvent validateAndCancelPayment(Payment payment,
                                                 CreditEntry creditEntry,
                                                 List<CreditHistory> creditHistoryList,
                                                 List<String> failureMessages) {
        payment.validatePayment(failureMessages);
        addCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistoryList, TransactionType.CREDIT);

        if (failureMessages.isEmpty()) {
            log.info("Payment is cancelled for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)));
        }

        log.info("Payment cancellation is failed for order id: {}", payment.getOrderId().getValue());
        payment.updateStatus(PaymentStatus.FAILED);
        return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages);
    }

    private void validateCreditEntry(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            log.error("Customer with id: {} doesn't have enough credit for payment!",
                payment.getCustomerId().getValue());
            failureMessages.add("Customer with id=" + payment.getCustomerId().getValue() + " doesn't have enough credit for payment!");
        }
    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private void updateCreditHistory(Payment payment, List<CreditHistory> creditHistoryList, TransactionType transactionType) {
        creditHistoryList.add(CreditHistory.builder()
                .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                .customerId(payment.getCustomerId())
                .amount(payment.getPrice())
                .transactionType(transactionType)
            .build());
    }

    private void validateCreditHistory(CreditEntry creditEntry, List<CreditHistory> creditHistoryList, List<String> failureMessages) {
        Money totalCreditHistory = getTotalHistoryAmount(creditHistoryList, TransactionType.CREDIT);
        Money totalDebitHistory = getTotalHistoryAmount(creditHistoryList, TransactionType.DEBIT);

        if (totalDebitHistory.isGreaterThan(totalCreditHistory)) {
            log.error("Customer with id: {} doesn't have enough credit according to credit history",
                creditEntry.getCustomerId().getValue());
            failureMessages.add("Customer with id="+ creditEntry.getCustomerId().getValue() +" doesn't have enough credit according to credit history");
        }

        Money subtractedPrice = totalCreditHistory.subtract(totalDebitHistory);
        if (!creditEntry.getTotalCreditAmount().equals(subtractedPrice)) {
            String errorMessage = "Credit history total is not equal to current credit for customer id: " +
                creditEntry.getCustomerId().getValue() + "! Total Credit Amount: " + creditEntry.getTotalCreditAmount().getAmount() +
                ", Total Subtracted Amount: " + subtractedPrice.getAmount();
            log.error(errorMessage);
            failureMessages.add(errorMessage);
        }
    }

    private Money getTotalHistoryAmount(List<CreditHistory> creditHistoryList, TransactionType transactionType) {
        return creditHistoryList.stream()
            .filter(creditHistory -> creditHistory.getTransactionType() == transactionType)
            .map(CreditHistory::getAmount)
            .reduce(Money.ZERO, Money::add);
    }

    private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }
}
