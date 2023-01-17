package org.food.ordering.system.payment.service.dataaccess.creditentry.adapter;

import lombok.RequiredArgsConstructor;
import org.food.ordering.system.domain.valueobject.CustomerId;
import org.food.ordering.system.payment.service.dataaccess.creditentry.mapper.CreditEntryDataAccessMapper;
import org.food.ordering.system.payment.service.dataaccess.creditentry.repository.CreditEntryJpaRepository;
import org.food.ordering.system.payment.service.domain.entity.CreditEntry;
import org.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CreditEntryRepositoryImpl implements CreditEntryRepository {

    private final CreditEntryJpaRepository creditEntryJpaRepository;
    private final CreditEntryDataAccessMapper creditEntryDataAccessMapper;

    @Override
    public CreditEntry save(CreditEntry creditEntry) {
        return creditEntryDataAccessMapper
                .creditEntryEntityToCreditEntry(creditEntryJpaRepository
                        .save(creditEntryDataAccessMapper.creditEntryToCreditEntryEntity(creditEntry)));
    }

    @Override
    public Optional<CreditEntry> findByCustomerId(CustomerId customerId) {
        return creditEntryJpaRepository
                .findByCustomerId(customerId.getValue())
                .map(creditEntryDataAccessMapper::creditEntryEntityToCreditEntry);
    }
}
