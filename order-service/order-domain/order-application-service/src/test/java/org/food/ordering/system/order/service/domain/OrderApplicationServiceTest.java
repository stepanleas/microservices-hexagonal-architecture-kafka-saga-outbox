package org.food.ordering.system.order.service.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.food.ordering.system.domain.valueobject.*;
import org.food.ordering.system.order.service.domain.application.dto.create.CreateOrderCommand;
import org.food.ordering.system.order.service.domain.application.dto.create.CreateOrderResponse;
import org.food.ordering.system.order.service.domain.application.dto.create.OrderAddress;
import org.food.ordering.system.order.service.domain.application.dto.create.OrderItem;
import org.food.ordering.system.order.service.domain.application.mapper.OrderDataMapper;
import org.food.ordering.system.order.service.domain.application.outbox.model.payment.OrderPaymentEventPayload;
import org.food.ordering.system.order.service.domain.application.outbox.model.payment.OrderPaymentOutboxMessage;
import org.food.ordering.system.order.service.domain.application.ports.input.service.OrderApplicationService;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.CustomerRepository;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.OrderRepository;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.PaymentOutboxRepository;
import org.food.ordering.system.order.service.domain.application.ports.output.repository.RestaurantRepository;
import org.food.ordering.system.order.service.domain.core.entity.Customer;
import org.food.ordering.system.order.service.domain.core.entity.Order;
import org.food.ordering.system.order.service.domain.core.entity.Product;
import org.food.ordering.system.order.service.domain.core.entity.Restaurant;
import org.food.ordering.system.order.service.domain.core.exception.OrderDomainException;
import org.food.ordering.system.outbox.OutboxStatus;
import org.food.ordering.system.saga.SagaStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.food.ordering.system.saga.order.SagaConstant.ORDER_SAGA_NAME;

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

    @Autowired
    private PaymentOutboxRepository paymentOutboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderCommand createOrderCommandWrongPrice;
    private CreateOrderCommand createOrderCommandWrongProductPrice;
    private final UUID CUSTOMER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41");
    private final UUID RESTAURANT_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb45");
    private final UUID PRODUCT_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb48");
    private final UUID ORDER_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afb");
    private final UUID SAGA_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afa");
    private final BigDecimal PRICE = new BigDecimal("200.00");

    @BeforeAll
    void init() {
        createOrderCommand = CreateOrderCommand.builder()
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .address(OrderAddress.builder()
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
            .address(OrderAddress.builder()
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
            .address(OrderAddress.builder()
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

        Customer customer = new Customer(new CustomerId(CUSTOMER_ID));

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
        Mockito.when(paymentOutboxRepository.save(Mockito.any(OrderPaymentOutboxMessage.class))).thenReturn(getOrderPaymentOutboxMessage());
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

    private OrderPaymentOutboxMessage getOrderPaymentOutboxMessage() {
        OrderPaymentEventPayload orderPaymentEventPayload = OrderPaymentEventPayload.builder()
            .orderId(ORDER_ID.toString())
            .customerId(CUSTOMER_ID.toString())
            .price(PRICE)
            .createdAt(ZonedDateTime.now())
            .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
            .build();

        return OrderPaymentOutboxMessage.builder()
            .id(UUID.randomUUID())
            .sagaId(SAGA_ID)
            .createdAt(ZonedDateTime.now())
            .type(ORDER_SAGA_NAME)
            .payload(createPayload(orderPaymentEventPayload))
            .orderStatus(OrderStatus.PENDING)
            .sagaStatus(SagaStatus.STARTED)
            .outboxStatus(OutboxStatus.STARTED)
            .version(0)
            .build();
    }

    private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderPaymentEventPayload);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Cannot create " + OrderPaymentEventPayload.class.getSimpleName() + " object!");
        }
    }
}
