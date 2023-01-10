package org.food.ordering.system.order.service.domain;

import org.food.ordering.system.domain.valueobject.*;
import org.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import org.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import org.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import org.food.ordering.system.order.service.domain.dto.create.OrderItem;
import org.food.ordering.system.order.service.domain.entity.Customer;
import org.food.ordering.system.order.service.domain.entity.Order;
import org.food.ordering.system.order.service.domain.entity.Product;
import org.food.ordering.system.order.service.domain.entity.Restaurant;
import org.food.ordering.system.order.service.domain.exception.OrderDomainException;
import org.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import org.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import org.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import org.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import org.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceTest {

    @Autowired
    private OrderApplicationService orderApplicationService;
    @Autowired
    private OrderDataMapper orderDataMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderCommand createOrderCommandWrongPrice;
    private CreateOrderCommand createOrderCommandWrongProductPrice;
    private final UUID CUSTOMER_ID = UUID.fromString("a185837d-b3c6-477c-b29e-014a3d85ce84");
    private final UUID RESTAURANT_ID = UUID.fromString("84276fc2-1ad3-4e52-b4fc-b45d924609c3");
    private final UUID PRODUCT_ID = UUID.fromString("373df12e-3b61-4867-aeb9-bc47cc9887e3");
    private final UUID ORDER_ID = UUID.fromString("e9ad27a5-eb96-43fc-aafa-31d9116f1dc5");
    private final BigDecimal PRICE = new BigDecimal("200.00");

    @BeforeAll
    void init() {
        createOrderCommand = CreateOrderCommand.builder()
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .orderAddress(OrderAddress.builder()
                .street("street_1")
                .postalCode("1000AB")
                .city("Paris")
                .build())
            .price(PRICE)
            .items(List.of(
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(1)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("50.00"))
                    .build(),
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(3)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("150.00"))
                    .build()))
            .build();

        createOrderCommandWrongPrice = CreateOrderCommand.builder()
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .orderAddress(OrderAddress.builder()
                .street("street_1")
                .postalCode("1000AB")
                .city("Paris")
                .build())
            .price(new BigDecimal("250.00"))
            .items(List.of(
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(1)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("50.00"))
                    .build(),
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(3)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("150.00"))
                    .build()))
            .build();

        createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .orderAddress(OrderAddress.builder()
                .street("street_1")
                .postalCode("1000AB")
                .city("Paris")
                .build())
            .price(new BigDecimal("210.00"))
            .items(List.of(
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(1)
                    .price(new BigDecimal("60.00"))
                    .subTotal(new BigDecimal("60.00"))
                    .build(),
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(3)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("150.00"))
                    .build()))
            .build();

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse = Restaurant.builder()
            .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
            .products(List.of(
                new Product(
                    new ProductId(PRODUCT_ID), "product-1",
                    new Money(new BigDecimal("50.00"))),
                new Product(
                    new ProductId(PRODUCT_ID), "product-2",
                    new Money(new BigDecimal("50.00")))
                ))
            .active(true)
            .build();

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        Mockito.when(customerRepository.findCustomer(CUSTOMER_ID))
            .thenReturn(Optional.of(customer));

        Mockito.when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
            .thenReturn(Optional.of(restaurantResponse));

        Mockito.when(orderRepository.save(Mockito.any(Order.class))).thenReturn(order);
    }

    @Test
    void testCreateOrder() {
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
        Assertions.assertEquals(OrderStatus.PENDING, createOrderResponse.getOrderStatus());
        Assertions.assertEquals("Order Created Successfully", createOrderResponse.getMessage());
        Assertions.assertNotNull(createOrderResponse.getOrderTrackingId());
    }

    @Test
    void testCreateOrderWithWrongTotalPrice() {
        OrderDomainException orderDomainException = Assertions.assertThrows(OrderDomainException.class, () -> {
            orderApplicationService.createOrder(createOrderCommandWrongPrice);
        });
        Assertions.assertEquals(
            "Total price: 250.00 is not equal to Order Items total: 200.00!",
            orderDomainException.getMessage());
    }

    @Test
    void testCreateOrderWithWrongProductPrice() {
        OrderDomainException orderDomainException = Assertions.assertThrows(OrderDomainException.class, () -> {
            orderApplicationService.createOrder(createOrderCommandWrongProductPrice);
        });
        Assertions.assertEquals(
            "Order Item price: 60.00 is not valid for product " + PRODUCT_ID,
            orderDomainException.getMessage()
        );
    }

    @Test
    void testCreateOrderWithPassiveRestaurant() {
        Restaurant restaurantResponse = Restaurant.builder()
            .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
            .products(List.of(
                new Product(
                    new ProductId(PRODUCT_ID), "product-1",
                    new Money(new BigDecimal("50.00"))),
                new Product(
                    new ProductId(PRODUCT_ID), "product-2",
                    new Money(new BigDecimal("50.00")))
            ))
            .active(false)
            .build();

        Mockito.when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
            .thenReturn(Optional.of(restaurantResponse));

        OrderDomainException orderDomainException = Assertions.assertThrows(OrderDomainException.class, () -> {
            orderApplicationService.createOrder(createOrderCommand);
        });

        Assertions.assertEquals(
            "Restaurant with id " + restaurantResponse.getId().getValue() + " is currently not active!",
            orderDomainException.getMessage());
    }
}
