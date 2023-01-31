package org.food.ordering.system.payment.service.domain.core;

import org.food.ordering.system.payment.service.domain.core.entity.CreditEntry;
import org.food.ordering.system.payment.service.domain.core.entity.CreditHistory;
import org.food.ordering.system.payment.service.domain.core.entity.Payment;
import org.food.ordering.system.payment.service.domain.core.event.PaymentEvent;

import java.util.List;

public interface PaymentDomainService {
    PaymentEvent validateAndInitiatePayment(Payment payment,
                                            CreditEntry creditEntry,
                                            List<CreditHistory> creditHistoryList,
                                            List<String> failureMessages);
    PaymentEvent validateAndCancelPayment(Payment payment,
                                          CreditEntry creditEntry,
                                          List<CreditHistory> creditHistoryList,
                                          List<String> failureMessages);
}
