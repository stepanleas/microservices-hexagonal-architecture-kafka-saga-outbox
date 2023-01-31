package org.food.ordering.system.order.service.container;

import org.food.ordering.system.order.service.domain.core.OrderDomainService;
import org.food.ordering.system.order.service.domain.core.OrderDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }
}
