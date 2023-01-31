package org.food.ordering.system.restaurant.service.domain.application.ports.output.repository;

import org.food.ordering.system.restaurant.service.domain.core.entity.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {
    Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
