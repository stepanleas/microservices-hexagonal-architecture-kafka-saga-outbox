package org.food.ordering.system.restaurant.service.container;

import org.food.ordering.system.restaurant.service.domain.core.RestaurantDomainService;
import org.food.ordering.system.restaurant.service.domain.core.RestaurantDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RestaurantDomainService restaurantDomainService() {
        return new RestaurantDomainServiceImpl();
    }
}
