package org.food.ordering.system.customer.service.container;

import org.food.ordering.customer.service.domain.core.CustomerDomainService;
import org.food.ordering.customer.service.domain.core.CustomerDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CustomerDomainService customerDomainService() {
        return new CustomerDomainServiceImpl();
    }
}
