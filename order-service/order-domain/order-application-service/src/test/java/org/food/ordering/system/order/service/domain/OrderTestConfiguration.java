package org.food.ordering.system.order.service.domain;

import org.food.ordering.system.order.service.domain.application.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import org.food.ordering.system.order.service.domain.application.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.*;
import org.food.ordering.system.order.service.domain.core.OrderDomainService;
import org.food.ordering.system.order.service.domain.core.OrderDomainServiceImpl;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "org.food.ordering.system")
public class OrderTestConfiguration {

    @Bean
    public PaymentRequestMessagePublisher paymentRequestMessagePublisher() {
       return Mockito.mock(PaymentRequestMessagePublisher.class);
    }

    @Bean
    public RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher() {
        return Mockito.mock(RestaurantApprovalRequestMessagePublisher.class);
    }

    @Bean
    public OrderRepository orderRepository() {
        return Mockito.mock(OrderRepository.class);
    }

    @Bean
    public CustomerRepository customerRepository() {
        return Mockito.mock(CustomerRepository.class);
    }

    @Bean
    public RestaurantRepository restaurantRepository() {
        return Mockito.mock(RestaurantRepository.class);
    }

    @Bean
    public PaymentOutboxRepository paymentOutboxRepository() {
        return Mockito.mock(PaymentOutboxRepository.class);
    }

    @Bean
    public ApprovalOutboxRepository approvalOutboxRepository() {
        return Mockito.mock(ApprovalOutboxRepository.class);
    }

    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }
}
