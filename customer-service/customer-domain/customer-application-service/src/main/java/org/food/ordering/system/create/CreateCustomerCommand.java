package org.food.ordering.system.create;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateCustomerCommand(@NotNull UUID customerId, @NotNull String username, @NotNull String firstName,
                                    @NotNull String lastName) {
}
