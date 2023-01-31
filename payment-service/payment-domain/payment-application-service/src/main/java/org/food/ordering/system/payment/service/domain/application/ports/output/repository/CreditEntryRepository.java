package org.food.ordering.system.payment.service.domain.application.ports.output.repository;

import org.food.ordering.system.domain.valueobject.CustomerId;
import org.food.ordering.system.payment.service.domain.core.entity.CreditEntry;

import java.util.Optional;

public interface CreditEntryRepository {
    CreditEntry save(CreditEntry creditEntry);
    Optional<CreditEntry> findByCustomerId(CustomerId id);
}
