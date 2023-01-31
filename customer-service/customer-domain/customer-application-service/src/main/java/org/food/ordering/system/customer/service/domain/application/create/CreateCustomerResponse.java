package org.food.ordering.system.customer.service.domain.application.create;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateCustomerResponse(@NotNull UUID customerId, @NotNull String message) {
}
