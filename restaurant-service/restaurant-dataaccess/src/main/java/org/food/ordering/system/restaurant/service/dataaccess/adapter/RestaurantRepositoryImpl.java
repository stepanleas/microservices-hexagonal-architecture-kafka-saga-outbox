package org.food.ordering.system.restaurant.service.dataaccess.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import org.food.ordering.system.dataaccess.restaurant.repository.RestaurantJpaRepository;
import org.food.ordering.system.restaurant.service.dataaccess.mapper.RestaurantDataAccessMapper;
import org.food.ordering.system.restaurant.service.domain.core.entity.Restaurant;
import org.food.ordering.system.restaurant.service.domain.application.ports.output.repository.RestaurantRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    @Override
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        List<UUID> restaurantProducts = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
        log.info("Received restaurant product ids: {} and restaurant id: {}", restaurantProducts, restaurant.getId().getValue());
        Optional<List<RestaurantEntity>> restaurantEntities = restaurantJpaRepository
            .findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), restaurantProducts);
        return restaurantEntities.map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
    }
}
