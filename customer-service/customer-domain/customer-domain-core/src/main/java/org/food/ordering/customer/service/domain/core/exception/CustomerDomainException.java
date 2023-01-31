package org.food.ordering.customer.service.domain.core.exception;

import org.food.ordering.system.domain.exception.DomainException;

public class CustomerDomainException extends DomainException {

    public CustomerDomainException(String message) {
        super(message);
    }
}
