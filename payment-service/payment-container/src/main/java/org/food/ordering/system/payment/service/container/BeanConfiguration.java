package org.food.ordering.system.payment.service.container;

import org.food.ordering.system.payment.service.domain.core.PaymentDomainService;
import org.food.ordering.system.payment.service.domain.core.PaymentDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public PaymentDomainService paymentDomainService() {
        return new PaymentDomainServiceImpl();
    }
}
