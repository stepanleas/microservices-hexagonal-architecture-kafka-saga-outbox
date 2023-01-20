package org.food.ordering.system.order.service.dataaccess.restaurant.adapter;

import lombok.RequiredArgsConstructor;
import org.food.ordering.system.dataaccess.restaurant.repository.RestaurantJpaRepository;
import org.food.ordering.system.order.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import org.food.ordering.system.order.service.domain.entity.Restaurant;
import org.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    @Override
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        List<UUID> restaurantProducts = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
        return restaurantJpaRepository.findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), restaurantProducts)
            .map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
    }
}
